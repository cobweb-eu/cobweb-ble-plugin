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

uint8_t flag = 0;

uint16_t handler = 0;

//char message[54];

void setup() 
{  
  USB.println(F("BLE_13 Example"));  

  // 0. Turn BLE module ON
  BLE.ON(SOCKET0);

}


void loop() 
{
  // 1. Make Waspmote visible to other BLE modules
  BLE.setDiscoverableMode(BLE_GAP_GENERAL_DISCOVERABLE);

  // 2. Make Waspmote connectable to any other BLE device
  BLE.setConnectableMode(BLE_GAP_UNDIRECTED_CONNECTABLE);
  USB.println(F("Waiting for incoming connections..."));
  
  char message[] = "NS1";//,0.12,0.09,0.80,999.91";
  //snprintf( message, sizeof(message), "NNE,0.18,0.22,0.99,2.42", RTC.getTime());
  char* r = message;
  
  uint8_t datalength = strlen(r);
  USB.print(F("Length of data message: "));
  USB.println(datalength);
  
  uint64_t res = BLE.writeLocalAttribute(48, r);// 2EEC
  USB.print(F("Outresult: "));
  USB.println(res);
  
  // 3. Wait for connection status event during 30 seconds. 
  flag = BLE.waitEvent(10000); 
}




















