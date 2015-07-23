/*
 *  ------------------ [BLE_13] - Characteristic notification - Slave ----
 *  Explanation: This example shows how indicate processes works.
 *  The program first make itself discoverable and connectable, waiting 
 *  for incoming connections. Once connected, waits for indication 
 *  subscribing events and, when they are found, the subscribed attribute is 
 *  written five times to allow the master receive notification events.
 *  Then, it waits till the indication acknowledge event fro the written value.
 *  Finally, it keeps waiting events till the connection is over.
 *
 *  Copyright (C) 2014 Libelium Comunicaciones Distribuidas S.L.
 *  http://www.libelium.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS ARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Version:		0.1
 *  Design:		David Gascón
 *  Implementation:	Javier Siscart
 */

#include <WaspBLE.h>
#include <WaspSensorAgr_v20.h>

uint8_t flag = 0;
uint16_t handler = 0;

// variable to store the number of pending pulses
int pendingPulses;

// Variable to store the anemometer value
float anemometer;
// Variable to store the pluviometer value
float pluviometer1; //mm in current hour 
float pluviometer2; //mm in previous hour
float pluviometer3; //mm in last 24 hours
// Variable to store the vane value
int vane;

int power;

char a_pwer[10] = {0};
char a_wind[10] = {0}; 
char a_anem[10] = {0}; 
char a_plu0[10] = {0}; 
char a_plu1[10] = {0}; 
char a_plu2[10] = {0}; 

void setup() 
{  
  USB.println(F("COBWEB BLE Weather Waspmote"));  

  // Activate Components
  SensorAgrv20.ON();  
  RTC.ON();
  BLE.ON(SOCKET0);

  USB.print(F("Time:"));
  USB.println(RTC.getTime());

  //04 is Wednesday.
  //RTC.setTime("22:07:15:04:12:58:00");

  //Switch away from Agriculture sleep//
  RTC.setAlarm1("00:00:00:00", RTC_ABSOLUTE, RTC_ALM1_MODE5);

  // 1. Make Waspmote visible to other BLE modules
  BLE.setDiscoverableMode(BLE_GAP_GENERAL_DISCOVERABLE);

  // 2. Make Waspmote connectable to any other BLE device
  BLE.setConnectableMode(BLE_GAP_UNDIRECTED_CONNECTABLE);
}


void loop() 
{
  BLE.setDiscoverableMode(BLE_GAP_GENERAL_DISCOVERABLE);
  BLE.setConnectableMode(BLE_GAP_UNDIRECTED_CONNECTABLE);

  /////////////////////////////////////////////
  // 2.1. check pluviometer interruption
  /////////////////////////////////////////////
  if( intFlag & PLV_INT)
  {
    USB.println(F("+++ PLV interruption +++"));

    pendingPulses = intArray[PLV_POS];

    USB.print(F("Number of pending pulses:"));
    USB.println( pendingPulses );

    for(int i=0 ; i<pendingPulses; i++)
    {
      // Enter pulse information inside class structure
      SensorAgrv20.storePulse();

      // decrease number of pulses
      intArray[PLV_POS]--;
    }

    // Clear flag
    intFlag &= ~(PLV_INT); 
  }

  measureSensors();

  prepareData();

  // 3. Wait for connection status event during 30 seconds.
  //flag = BLE.waitEvent(10000); 
  delay(15000);
}

void measureSensors()
{  

  USB.println(F("------------- Measurement process ------------------"));

  /////////////////////////////////////////////////////
  // 1. Reading sensors
  ///////////////////////////////////////////////////// 

  // Turn on the sensor and wait for stabilization and response time
  SensorAgrv20.setSensorMode(SENS_ON, SENS_AGR_ANEMOMETER);
  delay(10);

  // Read the anemometer sensor 
  anemometer = SensorAgrv20.readValue(SENS_AGR_ANEMOMETER);

  // Read the pluviometer sensor 
  pluviometer1 = SensorAgrv20.readPluviometerCurrent();
  pluviometer2 = SensorAgrv20.readPluviometerHour();
  pluviometer3 = SensorAgrv20.readPluviometerDay();

  // Read the vane sensor 
  vane = SensorAgrv20.readValue(SENS_AGR_VANE);

  power = PWR.getBatteryLevel();

  // Turn off the sensor
  SensorAgrv20.setSensorMode(SENS_OFF, SENS_AGR_ANEMOMETER);


  /////////////////////////////////////////////////////
  // 2. USB: Print the weather values through the USB
  /////////////////////////////////////////////////////

  USB.print(F("Power: "));
  USB.println( power );

  // Print the accumulated rainfall
  USB.print(F("Current hour accumulated rainfall (mm/h): "));
  USB.println( pluviometer1 );

  // Print the accumulated rainfall
  USB.print(F("Previous hour accumulated rainfall (mm/h): "));
  USB.println( pluviometer2 );

  // Print the accumulated rainfall
  USB.print(F("Last 24h accumulated rainfall (mm/day): "));
  USB.println( pluviometer3 );

  // Print the anemometer value
  USB.print(F("Anemometer: "));
  USB.print(anemometer);
  USB.println(F("km/h"));

  // Print the vane value
  char vane_str[10] = {
    0                };
  USB.print(F("Vane: "));
  switch(SensorAgrv20.vaneDirection)
  {
  case  SENS_AGR_VANE_N   :  
    snprintf( vane_str, sizeof(vane_str), "N" );
    break;
  case  SENS_AGR_VANE_NNE :  
    snprintf( vane_str, sizeof(vane_str), "NNE" );
    break;  
  case  SENS_AGR_VANE_NE  :  
    snprintf( vane_str, sizeof(vane_str), "NE" );
    break;    
  case  SENS_AGR_VANE_ENE :  
    snprintf( vane_str, sizeof(vane_str), "ENE" );
    break;      
  case  SENS_AGR_VANE_E   :  
    snprintf( vane_str, sizeof(vane_str), "E" );
    break;    
  case  SENS_AGR_VANE_ESE :  
    snprintf( vane_str, sizeof(vane_str), "ESE" );
    break;  
  case  SENS_AGR_VANE_SE  :  
    snprintf( vane_str, sizeof(vane_str), "SE" );
    break;    
  case  SENS_AGR_VANE_SSE :  
    snprintf( vane_str, sizeof(vane_str), "SSE" );
    break;   
  case  SENS_AGR_VANE_S   :  
    snprintf( vane_str, sizeof(vane_str), "S" );
    break; 
  case  SENS_AGR_VANE_SSW :  
    snprintf( vane_str, sizeof(vane_str), "SSW" );
    break; 
  case  SENS_AGR_VANE_SW  :  
    snprintf( vane_str, sizeof(vane_str), "SW" );
    break;  
  case  SENS_AGR_VANE_WSW :  
    snprintf( vane_str, sizeof(vane_str), "WSW" );
    break; 
  case  SENS_AGR_VANE_W   :  
    snprintf( vane_str, sizeof(vane_str), "W" );
    break;   
  case  SENS_AGR_VANE_WNW :  
    snprintf( vane_str, sizeof(vane_str), "WNW" );
    break; 
  case  SENS_AGR_VANE_NW  :  
    snprintf( vane_str, sizeof(vane_str), "WN" );
    break;
  case  SENS_AGR_VANE_NNW :  
    snprintf( vane_str, sizeof(vane_str), "NNW" );
    break;  
  default                 :  
    snprintf( vane_str, sizeof(vane_str), "error" );
    break;    
  }

  USB.print( vane_str );
  USB.print(" ");
  USB.println(vane);
  USB.println(F("----------------------------------------------------\n"));
}

void prepareData() {
  char* wnd = a_wind;
  char* ane = a_anem;
  char* pl0 = a_plu0;
  char* pl1 = a_plu1;
  char* pl2 = a_plu2;

  itoa(vane, wnd, 16);

  encode(  anemometer, ane, 01000, "%2s%2s", "Anem");
  encode(pluviometer1, pl0, 10000, "%3s%2s", "Plu0");
  encode(pluviometer2, pl1, 10000, "%3s%2s", "Plu1");
  encode(pluviometer3, pl2, 10000, "%3s%2s", "Plu2");

  // Documentation says maximum attribute size is 54 bytes. Anything over 21 fails to be written.
  char message[21] = {
    0            };//
  char* r = message;

  sprintf(message, "%s%s%s%s%s%c", wnd,ane,pl0,pl1,pl2,power);

  BLE.writeLocalAttribute(48, r);// 2EEC

  //  USB.println(results);
}

void encode(float num, char* out, uint64_t clearance, char* fmt ,char* message) {
  uint64_t base = ((uint64_t)num) % clearance ;
  uint64_t frac = ((uint64_t)(num*100))%100;

  char* top = "10000";
  char* bot = "10";

  itoa(base, top, 16);
  itoa(frac, bot, 16);

  sprintf(out, fmt, top,bot);

  //  USB.print(message);
  //  USB.print(" ]");

  //  USB.println(num);
  //  USB.print("b ");
  //  USB.println(base);
  //  USB.print("f ");
  //  USB.println(frac);
  //  USB.print(" Top  ");
  //  USB.println(top);
  //  USB.print(" Bot  ");
  //  USB.println(bot);

  //  USB.print(out);
  //  USB.println("[");
}






