/**
 * Copyright 2012-2013 StackMob
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stackmob.customcode;

import com.stackmob.core.customcode.CustomCodeMethod;
import com.stackmob.core.rest.ProcessedAPIRequest;
import com.stackmob.core.rest.ResponseToProcess;
import com.stackmob.sdkapi.SDKServiceProvider;
import com.stackmob.core.InvalidSchemaException;
import com.stackmob.core.DatastoreException;
import com.stackmob.sdkapi.*;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelloWorld implements CustomCodeMethod {

 @Override
  public String getMethodName() {
    return "CRUD_Create";
  }

  @Override
  public List<String> getParams() {
    // Please note that the strings `user` and `username` are unsuitable for parameter names
    return Arrays.asList("firstName","lastName","fullName","company","email","phone");
  }

  public ResponseToProcess execute(ProcessedAPIRequest request, 
        SDKServiceProvider serviceProvider) {
     String firstName = "";
     String lastName = "";
     String fullName = "";
     String company = "";
     String email = "";
     String phone = "";

    LoggerService logger = serviceProvider.getLoggerService(HelloWorld.class);
    // JSON object gets passed into the StackMob Logs
    logger.debug(request.getBody());

    // I'll be using these maps to print messages to console as feedback to the operation
    Map<String, SMValue> feedback = new HashMap<String, SMValue>();
    Map<String, String> errMap = new HashMap<String, String>();    
   JSONParser parser = new JSONParser();
    try {
      Object obj = parser.parse(request.getBody());
      JSONObject jsonObject = (JSONObject) obj;

      // Fetch the values passed in by the user from the body of JSON
      firstName = (String) jsonObject.get("firstName");
      lastName = (String) jsonObject.get("lastName");
      company = (String) jsonObject.get("company");
      email = (String) jsonObject.get("email");
      phone = (String) jsonObject.get("phone");
      fullName = (String) jsonObject.get("fullName");

    } catch (ParseException pe) {
      logger.error(pe.getMessage(), pe);
      return badRequestResponse(errMap);
    }      
   if (hasNulls(firstName.lastName,company,email,phone,fullName)){
      return badRequestResponse(errMap);
    }

    feedback.put("Nour", new SMString(firstName));
    feedback.put("Hesham", new SMString(lastName));
    feedback.put("Hospital", new SMString(company));
    feedback.put("nino@yahoo.com", new SMString(email));
    feedback.put("222222222", new SMString(phone));
    feedback.put("Nour Hesham", new SMString(fullName));

   DataService ds = serviceProvider.getDataService();
    try {
      ds.createObject("ContactPilot", new SMObject(feedback));
    }
    catch (InvalidSchemaException ise) {
      return internalErrorResponse("invalid_schema", ise, errMap);  // http 500 - internal server error
    }
    catch (DatastoreException dse) {
      return internalErrorResponse("datastore_exception", dse, errMap);  // http 500 - internal server error
    }

    return new ResponseToProcess(HttpURLConnection.HTTP_OK, feedback);
   
}
  public Boolean isEmpty(String str) {
    return (str == null || str.isEmpty());
  }

  public Boolean hasNulls(String... strings){
    for (String s : strings){
      if(isEmpty(s)){
        return true;
      }
    }
    return false;
  }

  public ResponseToProcess badRequestResponse(Map<String, String> map){
    map.put("error", "Please fill in all parameters correctly");
    return new ResponseToProcess(HttpURLConnection.HTTP_BAD_REQUEST, map);
  }

  public ResponseToProcess badRequestResponse(Map<String, String> map, String message){
    map.put("error", message);
    return new ResponseToProcess(HttpURLConnection.HTTP_BAD_REQUEST, map);
  }

  public ResponseToProcess internalErrorResponse(String message, Exception e, Map<String, String> map){
    map.put("error", message);
    map.put("detail", e.toString());
    return new ResponseToProcess(HttpURLConnection.HTTP_INTERNAL_ERROR, map);
  }
}
