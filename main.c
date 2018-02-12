#include <stdio.h>
#include <string.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/event_groups.h"
#include "esp_system.h"
#include "esp_wifi.h"
#include "esp_event_loop.h"
#include "esp_log.h"
#include <lwip/sockets.h>
#include <nvs_flash.h>
#include <sys/types.h>
#include <sys/socket.h>
// set AP CONFIG values
#ifdef CONFIG_AP_HIDE_SSID
	#define CONFIG_AP_SSID_HIDDEN 1
#else
	#define CONFIG_AP_SSID_HIDDEN 0
#endif	
#ifdef CONFIG_WIFI_AUTH_OPEN
	#define CONFIG_AP_AUTHMODE WIFI_AUTH_OPEN
#endif
#ifdef CONFIG_WIFI_AUTH_WEP
	#define CONFIG_AP_AUTHMODE WIFI_AUTH_WEP
#endif
#ifdef CONFIG_WIFI_AUTH_WPA_PSK
	#define CONFIG_AP_AUTHMODE WIFI_AUTH_WPA_PSK
#endif
#ifdef CONFIG_WIFI_AUTH_WPA2_PSK
	#define CONFIG_AP_AUTHMODE WIFI_AUTH_WPA2_PSK
#endif
#ifdef CONFIG_WIFI_AUTH_WPA_WPA2_PSK
	#define CONFIG_AP_AUTHMODE WIFI_AUTH_WPA_WPA2_PSK
#endif
#ifdef CONFIG_WIFI_AUTH_WPA2_ENTERPRISE
	#define CONFIG_AP_AUTHMODE WIFI_AUTH_WPA2_ENTERPRISE
#endif
#define PORT 5555

// Event group
static EventGroupHandle_t event_group;
const int STA_CONNECTED_BIT = BIT0;
const int STA_DISCONNECTED_BIT = BIT1;
//const int port = 5555;
char name[20];
static int server_socket = 0;

// AP event handler
static esp_err_t event_handler(void *ctx, system_event_t *event)
{
    switch(event->event_id) {
		
    case SYSTEM_EVENT_AP_START:
		printf("Access point started\n");
		break;
		
	case SYSTEM_EVENT_AP_STACONNECTED:
		xEventGroupSetBits(event_group, STA_CONNECTED_BIT);
		break;

	case SYSTEM_EVENT_AP_STADISCONNECTED:
		xEventGroupSetBits(event_group, STA_DISCONNECTED_BIT);
		break;		
    
	default:
        break;
    }
   
	return ESP_OK;
}

bool handle_messages(int connect_socket) {
    // remember to return status
    bool ok = true;
    int num_dev;

    // read message
    char buf[10];
    //char name[20];
    memset(buf ,0 , 10);
    if (!recv(connect_socket, buf, 10, 0)) {
   	printf("!!!!!\n");
        return !ok;
    }
    num_dev = atoi(buf);
    switch(num_dev){
    	case 0: /*ap_config.ap.ssid = "microwave oven";*/
    		strcpy(name, "microwave oven");
    		break;
    	case 1: strcpy(name, "refrigerator");
    		break;
    	case 2: strcpy(name, "electric kettle");
    		break;
    	case 3: strcpy(name, "iron");
    		break;
    	case 4: strcpy(name, "electric radiator");
    		break;
    	case 5: strcpy(name, "electric stove");
    		break;
    }
    memset(buf ,0 , 10);
    if (!recv(connect_socket, buf, 10, 0)) {
   	printf("!!!!!\n");
        return !ok;
    }
    
    printf("%s", buf);
    return ok;

}

void start_socket_server() {
	int connect_socket;
    // create and listen on the socket
    server_socket = socket(AF_INET, SOCK_STREAM, 0);
    struct sockaddr_in server_address = {
        .sin_family = AF_INET,
        .sin_addr.s_addr = htonl(INADDR_ANY),
        .sin_port = htons(PORT)
    };
    
    //inet_aton("192.168.10.1", &server_address.sin_addr.s_addr);
    if (bind(server_socket, (struct sockaddr *)&server_address, sizeof(server_address )) < 0) {
    printf("Bind\n");
    }
    printf("success_bind\n");
    
    if (listen(server_socket, 5) < 0) 
    {
    	printf("pizdec");
    }
    printf("success_listen\n");
    //connect_socket = accept(server_socket, (struct sockaddr *)&client_addr, &socklen);
    connect_socket = accept(server_socket, NULL, NULL);
    if(connect_socket < 0){
    printf("WTF_ACCEPT\n");
    }
    printf("sc_accept\n");
    

    // handle messages
    while (1) {
    //printf("int %d", sock);
        if(handle_messages(connect_socket)){
        close(connect_socket);
        break;
        }
        
    }
}



// Main application
void app_main()
{	
	
	// disable the default wifi logging
	esp_log_level_set("wifi", ESP_LOG_NONE);
	
	// create the event group to handle wifi events
	event_group = xEventGroupCreate();
		
	// initialize the tcp stack
	tcpip_adapter_init();

	// stop DHCP server
	ESP_ERROR_CHECK(tcpip_adapter_dhcps_stop(TCPIP_ADAPTER_IF_AP));
	
	// assign a static IP to the network interface
	tcpip_adapter_ip_info_t info;
    memset(&info, 0, sizeof(info));
	IP4_ADDR(&info.ip, 192, 168, 10, 1);
    IP4_ADDR(&info.gw, 192, 168, 10, 1);
    IP4_ADDR(&info.netmask, 255, 255, 255, 0);
	ESP_ERROR_CHECK(tcpip_adapter_set_ip_info(TCPIP_ADAPTER_IF_AP, &info));
	
	// start the DHCP server   
    ESP_ERROR_CHECK(tcpip_adapter_dhcps_start(TCPIP_ADAPTER_IF_AP));
	
	// initialize the wifi event handler
	ESP_ERROR_CHECK(esp_event_loop_init(event_handler, NULL));
	
	// initialize the wifi stack in AccessPoint mode with config in RAM
	wifi_init_config_t wifi_init_config = WIFI_INIT_CONFIG_DEFAULT();
	ESP_ERROR_CHECK(esp_wifi_init(&wifi_init_config));
	ESP_ERROR_CHECK(esp_wifi_set_storage(WIFI_STORAGE_RAM));
	ESP_ERROR_CHECK(esp_wifi_set_mode(WIFI_MODE_AP));

	// configure the wifi connection and start the interface
	//printf("%s\n", CONFIG_AP_SSID);
	
	strcpy(name, CONFIG_AP_SSID);
	//strcpy(CONFIG_AP_SSID, name);
	//strcpy(CONFIG_AP_SSID, name);
	//printf("%s\n",CONFIG_AP_SSID);
	wifi_config_t ap_config = {
        .ap = {
            //.ssid = CONFIG_AP_SSID,
            .password = CONFIG_AP_PASSWORD,
			.ssid_len = 0,
			.channel = CONFIG_AP_CHANNEL,
			.authmode = CONFIG_AP_AUTHMODE,
			.ssid_hidden = CONFIG_AP_SSID_HIDDEN,
			.max_connection = CONFIG_AP_MAX_CONNECTIONS,
			.beacon_interval = CONFIG_AP_BEACON_INTERVAL,			
        },
    };
	while(1){

    	strcpy((char*)(ap_config.ap.ssid), name);
	ESP_ERROR_CHECK(esp_wifi_set_config(WIFI_IF_AP, &ap_config));
    
	// start the wifi interface
	ESP_ERROR_CHECK(esp_wifi_start());
	printf("Starting access point, SSID=%s\n", CONFIG_AP_SSID);
	start_socket_server();
	printf("Retry\n");
	}
    	vTaskDelete(NULL);
}
