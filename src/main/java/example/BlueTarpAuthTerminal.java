package example;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class BlueTarpAuthTerminal {
  private static final int ONE_MINUTE_IN_MILLIS = 60000;

  private Long merchantNumber;
  private String merchantTerminalName;
  private String baseAuthUrl;
  private String authHeader;

  public BlueTarpAuthTerminal(Long merchantNumber, String merchantTerminalName, String clientKey, String baseAuthUrl) {
    this.merchantNumber = merchantNumber;
    this.merchantTerminalName = merchantTerminalName;
    if (!baseAuthUrl.endsWith("/")) {
      baseAuthUrl += "/";
    }
    this.baseAuthUrl = baseAuthUrl + merchantNumber;
    this.authHeader = "Bearer " + clientKey;
  }

  public String sendLookupRequest(String requestString){
    String response = null;
    try {
      URL requestUrl = new URL(baseAuthUrl + requestString);
      HttpURLConnection authCon = (HttpURLConnection)requestUrl.openConnection();
      authCon.setRequestMethod("GET");
      authCon.setConnectTimeout(ONE_MINUTE_IN_MILLIS);
      authCon.setRequestProperty("Authorization", authHeader);
      authCon.connect();

      System.out.println("Sending GET to: " + baseAuthUrl + requestString);
      int responseCode = authCon.getResponseCode();
      if(responseCode != HttpURLConnection.HTTP_OK){
        System.out.println("There was a problem connecting: " + authCon.getResponseMessage());
      } else {
        InputStream inputStream = authCon.getInputStream();
        response = readConnectionStream(inputStream);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return response;
  }

  public String sendAuthRequest(String requestXML){
    String response = null;
    try {
      URL requestUrl = new URL(baseAuthUrl);
      HttpURLConnection authCon = (HttpURLConnection)requestUrl.openConnection();
      authCon.setRequestMethod("POST");
      authCon.setConnectTimeout(ONE_MINUTE_IN_MILLIS);
      authCon.setRequestProperty("Authorization", authHeader);
      authCon.addRequestProperty("Content-Type", "text/xml");
      authCon.addRequestProperty("Accept", "text/xml");
      authCon.setDoOutput(true);
      OutputStreamWriter wr = new OutputStreamWriter(authCon.getOutputStream(), "UTF-8");
      wr.write(requestXML);
      wr.close();


      int responseCode = authCon.getResponseCode();
      if(responseCode != HttpURLConnection.HTTP_OK){
        System.out.println("There was a problem connecting: " + authCon.getResponseMessage());
      } else {
        InputStream inputStream = authCon.getInputStream();
        response = readConnectionStream(inputStream);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return response;
  }

  private String readConnectionStream(InputStream is){
    StringBuffer result = new StringBuffer();
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    String line;
    try {
      while((line = br.readLine()) != null){
         result.append(line);
      }
    } catch (IOException e){
      System.out.println("Error reading response");
    }
    return result.toString();
  }

  public Long getMerchantNumber() {return  merchantNumber; }
  public String getMerchantTerminalName() { return merchantTerminalName; }

}
