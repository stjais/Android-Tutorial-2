package com.aptech.findfriend.network;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

public class RestClient {

    private static int DEFAULT_TIMEOUT = 20 * 1000;

    private String link;
    private ArrayList<String> keys, values;

    /**
     * @param link link
     * @throws MalformedURLException MalformedURLException
     */
    public RestClient(String link, ArrayList<String> keys, ArrayList<String> values) throws MalformedURLException {

        this.link = link;
        this.keys = keys;
        this.values = values;
    }

    /**
     * @param is is
     * @return convertStreamToString
     */
    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    /**
     * @param params params
     * @return getPostDataString
     * @throws Exception Exception
     */
    private String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while (itr.hasNext()) {

            String key = itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }

    /**
     * @param method method
     * @return execute
     * @throws Exception Exception
     */
    public String execute(RequestMethod method) throws Exception {

        String result = null;
        URL url;
        HttpURLConnection urlConnection;
        int responseCode;
        JSONObject jsonObject;

        switch (method) {
            case GET:
                link = link + "?";
                for (int i = 0; i < keys.size(); i++) {
                    if (i == keys.size() - 1)
                        link = link + keys.get(i) + "=" + values.get(i) + "";
                    else
                        link = link + keys.get(i) + "=" + values.get(i) + "&";
                }

                url = new URL(link);
                url = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(DEFAULT_TIMEOUT);
                urlConnection.setConnectTimeout(DEFAULT_TIMEOUT);
                urlConnection.setRequestMethod("GET");

                urlConnection.connect();

                responseCode = urlConnection.getResponseCode();
                jsonObject = new JSONObject();
                jsonObject.put("responseCode", responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    jsonObject.put("responseMessage", convertStreamToString(urlConnection.getInputStream()));
                } else {
                    jsonObject.put("responseMessage", "");
                }
                result = jsonObject.toString();
                break;
            case POST:
                url = new URL(link);
                url = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(DEFAULT_TIMEOUT);
                urlConnection.setConnectTimeout(DEFAULT_TIMEOUT);
                urlConnection.setRequestMethod("POST");

                // adding parameters
                JSONObject params = new JSONObject();
                for (int i = 0; i < keys.size(); i++) {
                    params.put(keys.get(i), values.get(i));
                }
                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(params));
                writer.flush();
                writer.close();
                os.close();

                responseCode = urlConnection.getResponseCode();
                jsonObject = new JSONObject();
                jsonObject.put("responseCode", responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    jsonObject.put("responseMessage", convertStreamToString(urlConnection.getInputStream()));
                } else {
                    jsonObject.put("responseMessage", "");
                }
                result = jsonObject.toString();
                break;
            case PUT:

                break;
            case DELETE:

                break;
        }
        return result;
    }

    public enum RequestMethod {
        GET,
        POST,
        PUT,
        DELETE
    }


}
