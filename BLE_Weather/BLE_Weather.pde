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

char anem[10] = {0};
char plu1[10] = {0};
char plu2[10] = {0};
char plu3[10] = {0};

int power;

unsigned long epoch;
timestamp_t   timestamp;

// Documentation says maximum attribute size is 54 bytes. Anything over 21 fails to be written.
uint8_t message[21] = {
  0};

char vane_str[10] = { 0 };
char filename[20];
char data[128];

void setup() 
{  
  USB.println(F("COBWEB BLE Weather Waspmote"));  

  // Activate Components
  SensorAgrv20.ON();  
  RTC.ON();
  BLE.ON(SOCKET0);

  SensorAgrv20.attachPluvioInt();

  USB.print(F("Time:"));
  USB.println(RTC.getTime());

  //04 is Wednesday.
  //RTC.setTime("10:08:15:02:12:22:00");

  //Switch away from Agriculture sleep//
  //RTC.setAlarm1("00:00:00:00", RTC_ABSOLUTE, RTC_ALM1_MODE5);

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
    SensorAgrv20.attachPluvioInt();
  }

  measureSensors();
  printData();
  saveDataToFile();
  prepareData();

  RTC.breakTimeAbsolute( epoch, &timestamp ); 

  //  USB.print(F("Current hour num: "));
  //  USB.println(timestamp.hour, DEC);
  if( timestamp.hour >= 21 || timestamp.hour < 6 ) {
    USB.println(F("Time after 22 or before 6"));
    BLE.OFF();

    //MODE5 - Seconds match.
    //MODE4 - Minutes and seconds match.
    //SensorAgrv20.sleepAgr("00:00:00:00", RTC_ABSOLUTE, RTC_ALM1_MODE4, SENS_OFF, SENS_AGR_PLUVIOMETER);
    delay(3600000); //Sleep an hour.
    delay(500);
    BLE.ON(SOCKET0);
    delay(500);
    BLE.setDiscoverableMode(BLE_GAP_GENERAL_DISCOVERABLE);
    BLE.setConnectableMode(BLE_GAP_UNDIRECTED_CONNECTABLE);
  }
  else {
    BLE.sleep();
    delay(60000);
    BLE.wakeUp();
  }
}

void measureSensors()
{  

  USB.println(F("------------- Measurement process ------------------"));

  /////////////////////////////////////////////////////
  // 1. Reading sensors
  ///////////////////////////////////////////////////// 

  // Turn on the sensor and wait for stabilization and response time
  RTC.ON();
  SensorAgrv20.setSensorMode(SENS_ON, SENS_AGR_ANEMOMETER);
  delay(10);

  // Read the anemometer sensor 
  anemometer = SensorAgrv20.readValue(SENS_AGR_ANEMOMETER);

  // Read the pluviometer sensor 
  pluviometer1 = SensorAgrv20.readPluviometerCurrent();
  pluviometer2 = SensorAgrv20.readPluviometerHour();
  pluviometer3 = SensorAgrv20.readPluviometerDay();

  // Read the vane sensor 
  /*vane = */  SensorAgrv20.readValue(SENS_AGR_VANE);

  power = PWR.getBatteryLevel();

  epoch = RTC.getEpochTime();
  RTC.getTime();

  // Turn off the sensor
  SensorAgrv20.setSensorMode(SENS_OFF, SENS_AGR_ANEMOMETER);
  RTC.OFF();
}

void printData()
{
  /////////////////////////////////////////////////////
  // 2. USB: Print the weather values through the USB
  /////////////////////////////////////////////////////

  USB.print(F("Power: "));
  USB.println( power );

  USB.print(F("Epoch: "));
  USB.println( epoch );

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

  USB.print(F("Vane: "));
  switch(SensorAgrv20.vaneDirection)
  {
  case  SENS_AGR_VANE_N   :  
    snprintf( vane_str, sizeof(vane_str), "N" );
    vane = 0;
    break;
  case  SENS_AGR_VANE_NNE :  
    snprintf( vane_str, sizeof(vane_str), "NNE" );
    vane = 1;
    break;  
  case  SENS_AGR_VANE_NE  :  
    snprintf( vane_str, sizeof(vane_str), "NE" );
    vane = 2;
    break;    
  case  SENS_AGR_VANE_ENE :  
    snprintf( vane_str, sizeof(vane_str), "ENE" );
    vane = 3;
    break;      
  case  SENS_AGR_VANE_E   :  
    snprintf( vane_str, sizeof(vane_str), "E" );
    vane = 4;
    break;    
  case  SENS_AGR_VANE_ESE :  
    snprintf( vane_str, sizeof(vane_str), "ESE" );
    vane = 5;
    break;  
  case  SENS_AGR_VANE_SE  :  
    snprintf( vane_str, sizeof(vane_str), "SE" );
    vane = 6;
    break;    
  case  SENS_AGR_VANE_SSE :  
    snprintf( vane_str, sizeof(vane_str), "SSE" );
    vane = 7;
    break;   
  case  SENS_AGR_VANE_S   :  
    snprintf( vane_str, sizeof(vane_str), "S" );
    vane = 8;
    break; 
  case  SENS_AGR_VANE_SSW :  
    snprintf( vane_str, sizeof(vane_str), "SSW" );
    vane = 9;
    break; 
  case  SENS_AGR_VANE_SW  :  
    snprintf( vane_str, sizeof(vane_str), "SW" );
    vane = 10;
    break;  
  case  SENS_AGR_VANE_WSW :  
    snprintf( vane_str, sizeof(vane_str), "WSW" );
    vane = 11;
    break; 
  case  SENS_AGR_VANE_W   :  
    snprintf( vane_str, sizeof(vane_str), "W" );
    vane = 12;
    break;   
  case  SENS_AGR_VANE_WNW :  
    snprintf( vane_str, sizeof(vane_str), "WNW" );
    vane = 13;
    break; 
  case  SENS_AGR_VANE_NW  :  
    snprintf( vane_str, sizeof(vane_str), "NW" );
    vane = 14;
    break;
  case  SENS_AGR_VANE_NNW :  
    snprintf( vane_str, sizeof(vane_str), "NNW" );
    vane = 15;
    break;  
  default                 :  
    snprintf( vane_str, sizeof(vane_str), "error" );
    break;    
  }

  USB.print( vane_str );
  USB.print(F(" "));
  USB.println(vane);
  USB.println(F("----------------------------------------------------\n"));
}

void prepareData() {
  // Vane   1 byte
  // Anem   3 bytes (2.5 bytes technically)
  // Pluvio 3 bytes 
  // Power  1 byte
  // Epoch  4 bytes

  // 1 + 1 + 3 + 3*3 + 4 = 14 + 4

  //  USB.println(F("Encoding"));

  message[0] = vane;
  encode(  anemometer,  message,  1, "Anem");
  encode(pluviometer1,  message,  4, "Plu0");
  encode(pluviometer2,  message,  7, "Plu1");
  encode(pluviometer3,  message, 10, "Plu2");
  message[13] = power;
  time(epoch       ,  message, 14, "Time");

  uint8_t* r = message;

  BLE.writeLocalAttribute(48, r, 21);// 2EEC

  //  USB.println(F("Message: "));
  //  for(int i = 0; i<21;i++) {
  //    USB.print( message[i], HEX);
  //    USB.println();
  //  }
  //  USB.println(F("EOM"));
  //  USB.println(results);
}

void encode(float num, uint8_t out[], int offset ,char* message) {
  //Length 3.
  int encoded = (int)(num*100);

  int p1 = (int)((encoded>>16) & 0xFF);
  int p2 = (int)((encoded>> 8) & 0xFF);
  int p3 = (int)((encoded    ) & 0xFF);

  out[offset + 0] = p1;
  out[offset + 1] = p2;
  out[offset + 2] = p3;

  //  USB.println(message);
  //  USB.println(encoded);
  //  USB.println(p1);
  //  USB.println(p2);
  //  USB.println(p3);
}

void time(unsigned long num, uint8_t out[], int offset, char* message) {
  //Length 4.

  int p1 = (int)((num>>24) & 0xFF);
  int p2 = (int)((num>>16) & 0xFF);
  int p3 = (int)((num>> 8) & 0xFF);
  int p4 = (int)((num    ) & 0xFF);

  out[offset + 0] = p1;
  out[offset + 1] = p2;
  out[offset + 2] = p3;
  out[offset + 3] = p4;

  //  USB.println(message);
  //  USB.println(p1);
  //  USB.println(p2);
  //  USB.println(p3);
  //  USB.println(p4);
}

void saveDataToFile() {
  SD.ON();

  sprintf(filename,"/%02u/%02u",RTC.year, RTC.month);
  SD.mkdir(filename);

  sprintf(filename,"/%02u/%02u/%02u%02u%02u.TXT",RTC.year, RTC.month, RTC.year, RTC.month, RTC.date);
  SD.create(filename);
  USB.print(F("File written: "));
  USB.println(filename);
  
  Utils.float2String (anemometer, anem, 2);
  Utils.float2String (pluviometer1, plu1, 2);  
  Utils.float2String (pluviometer2, plu2, 2);
  Utils.float2String (pluviometer3, plu3, 2);

  sprintf(data, "%s,%s,%s,%s,%s,%u,%lu", vane_str, anem, plu1, plu2, plu3, power, epoch);
  SD.appendln(filename,data);
  USB.println(data);

  SD.OFF();
}













