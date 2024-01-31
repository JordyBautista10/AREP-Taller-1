package org.example;

import java.net.*;
import java.io.*;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        HashMap<String, String> cache = new HashMap<String, String>();

        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }
        boolean running = true;
        while (running) {
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            clientSocket.getInputStream()));
            String inputLine, outputLine;

            boolean firstLine = true;
            String uriStr ="";

            while ((inputLine = in.readLine()) != null) {
                if(firstLine){
                    uriStr = inputLine.split(" ")[1];
                    firstLine = false;
                }
                System.out.println("Received: " + inputLine);
                if (!in.ready()) {
                    break;
                }
            }
            if(uriStr.startsWith("/Cliente")){
                outputLine = httpClientHtml();
            }else if (uriStr.startsWith("/Busqueda") && uriStr.length() > 12){
                String nameMovie = uriStr.substring(12).toLowerCase();
                if (!cache.containsKey(nameMovie)){
                    System.out.println("No se encontro en el cache");
                    outputLine = makeRequest("http://www.omdbapi.com/?apikey=b1060e61&t=" + nameMovie);
                    cache.put(nameMovie, outputLine);
                } else {
                    System.out.println("Se encontro en el cache Bien");
                    outputLine = cache.get(nameMovie);
                }
                //Cache
                System.out.println("-----------------------: " + uriStr + outputLine);
            }
            else {
                outputLine = httpError();
            }
            out.println(outputLine);
            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }

     public static String makeRequest(String url) throws IOException {

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");

        //The following invocation perform the connection implicitly before getting the code
        int responseCode = con.getResponseCode();
        StringBuffer response = new StringBuffer();
        System.out.println("GET Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;

            response.append("HTTP/1.1 200 OK\r\n" + "Content-Type:application/json\r\n" + "\r\n"); // Encabezado
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            System.out.println(response.toString());
        } else {
            System.out.println("GET request not worked");
        }
        System.out.println("GET DONE");

        return response.toString();
    }

    public static String httpError() {
        return "HTTP/1.1 400 Not found\r\n" //encabezado necesario
                + "Content-Type:text/html\r\n"
                + "\r\n" //retorno de carro y salto de linea
                + "<!DOCTYPE html>\n" +
                "<html lang=\"es\">\n" +
                "<head>\n" +
                "</head>\n" +
                "<body>\n" +
                "</body>\n" +
                "</html>\n";
    }

    public static String httpClientHtml() {
        return "HTTP/1.1 200 OK\r\n" //encabezado necesario
                + "Content-Type:text/html\r\n"
                + "\r\n" //retorno de carro y salto de linea
                + "<!DOCTYPE html>"
                + "<html>\n"
                + "    <head>\n"
                + "        <title>Form Example</title>\n"
                + "        <meta charset=\"UTF-8\">\n"
                + "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "    </head>\n"
                + "    <body>\n"
                + "        <h1>Form with GET</h1>\n"
                + "        <form action=\"/Busqueda\">\n"
                + "            <label for=\"name\">Name:</label><br>\n"
                + "            <input type=\"text\" id=\"name\" name=\"t\" value=\"John\"><br><br>\n"
                + "            <input type=\"button\" value=\"Submit\" onclick=\"loadGetMsg()\">\n"
                + "        </form> \n"
                + "        <div id=\"getrespmsg\"></div>\n"
                + "\n"
                + "        <script>\n"
                + "            function loadGetMsg() {\n"
                + "                let nameVar = document.getElementById(\"name\").value;\n"
                + "                const xhttp = new XMLHttpRequest();\n"
                + "                xhttp.onload = function() {\n"
                + "                    var jsonResponse = JSON.parse(this.responseText);"
                + "                    var htmlContent = \"<b>Title: </b>\" + jsonResponse.Title + \"<br>\";"
                + "                    htmlContent += \"<b> Year: </b>\" + jsonResponse.Year + \"<br>\";"
                + "                    htmlContent += \"<b> RatedRated: </b>\" + jsonResponse.Rated + \"<br>\";"
                + "                    document.getElementById(\"getrespmsg\").innerHTML = htmlContent \n"
                + "                }\n"
                + "                xhttp.open(\"GET\", \"/Busqueda?t=\"+nameVar);\n"
                + "                xhttp.send();\n"
                + "            }\n"
                + "        </script>\n"
                + "\n"
                + "        <h1>Form with POST</h1>\n"
                + "        <form action=\"/hellopost\">\n"
                + "            <label for=\"postname\">Name:</label><br>\n"
                + "            <input type=\"text\" id=\"postname\" name=\"name\" value=\"John\"><br><br>\n"
                + "            <input type=\"button\" value=\"Submit\" onclick=\"loadPostMsg(postname)\">\n"
                + "        </form>\n"
                + "        \n"
                + "        <div id=\"postrespmsg\"></div>\n"
                + "        \n"
                + "        <script>\n"
                + "            function loadPostMsg(name){\n"
                + "                let url = \"/hellopost?name=\" + name.value;\n"
                + "\n"
                + "                fetch (url, {method: 'POST'})\n"
                + "                    .then(x => x.text())\n"
                + "                    .then(y => document.getElementById(\"postrespmsg\").innerHTML = y);\n"
                + "            }\n"
                + "        </script>\n"
                + "    </body>\n"
                + "</html>";
    }

}