package co.com;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@Component
public class Main
{
    final static String URL_TOKEN = "http://localhost:8080/token";
    final static String URL_AFILIACIONES = "http://localhost:8080/AfiliacionARL";
    final static String URL_EMPRESAS = "http://localhost:8080/ConsultaEmpresasTrasladadas";

    final static String CONTENT_TYPE = "Content-Type";
    final static String AUTHORIZATION = "Authorization";
    final static String BEARER = "bearer ";
    final static String CONTENT_TYPE_JSON = "application/json";
    final static String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";

    static Logger log = LoggerFactory.getLogger(Main.class);
    static ConfigFile configFile;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(cron = "0/1 * * * * *")
    public void prueba()
    {
        log.info("CRON PRUEBA. El tiempo es {}", dateFormat.format(new Date()));
    }

    @Scheduled(cron = "0/45 * * * * *")
    public void task()
    {
        log.info("El tiempo es {}", dateFormat.format(new Date()));
        log.info("Empezamos con el flujo...");
        try
        {
            log.info("Consultamos properties...");
            configFile = new ConfigFile();
            log.info("Properties consultado correctamente!");
            configFile.printStringProp();
            log.info("Token...");
            String token = token();
            log.info("Consumo token exitoso!");
            log.info(token);
            log.info("Afiliaciones...");
            JSONObject afiliaciones = afiliaciones(token);
            log.info("Consumo afiliaciones exitoso!");
            log.info(afiliaciones.toString());
            log.info("Consulta empresa...");
            JSONObject consultaEmpresas = consultaEmpresas(token);
            log.info("Consumo consultaEmpresa exitoso!");
            log.info(consultaEmpresas.toString());
            log.info("Consulta estructura empresa...");
            JSONObject consultaEstructuras = consultaEstructuraEmpresas(token);
            log.info("Consumo consultaEstructuraEmpresa exitoso!");
            log.info(consultaEstructuras.toString());
        } catch (IOException e)
        {
            log.error("Error en el flujo: ".concat(e.getMessage()));
        }
    }

    /**
     *
     * @return
     * @throws IOException
     */
    private String token() throws IOException
    {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        HttpPost post = new HttpPost(URL_TOKEN);
        post.setHeader(CONTENT_TYPE, "application/x-www-form-urlencoded");
        try (CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(post))
        {
            int status_code = response.getStatusLine().getStatusCode();
            if(status_code >= 200 && status_code <= 204)
            {
                String json_string = EntityUtils.toString(response.getEntity());
                JSONObject result = new JSONObject(json_string);
                return result.getString("access_token");
            }else
            {
                throw new IllegalStateException("Error en el token: ".concat(status_code + ""));

            }
        }
    }

    /**
     *
     * @param token
     * @return
     * @throws IOException
     */
    private JSONObject afiliaciones(String token) throws IOException
    {
        HttpPost post = new HttpPost(URL_AFILIACIONES);
        post.setHeader(CONTENT_TYPE, CONTENT_TYPE_JSON);
        post.setHeader(AUTHORIZATION, BEARER.concat(token));
        StringBuilder sb = new StringBuilder();
        try (CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(post)) {
            int status_code = response.getStatusLine().getStatusCode();
            if(status_code >= 200 && status_code <= 204)
            {
                if(response.getEntity().getContentLength() > 0)
                {
                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(response.getEntity().getContent()), 65728);
                    String line = null;
                    while ((line = reader.readLine()) != null)
                    {
                        sb.append(line);
                    }

                }

                return new JSONObject(sb.toString());

            }else
            {
                throw new IllegalStateException("Error en las afiliaciones: ".concat(status_code + ""));
            }
        }
    }


    /**
     *
     * @return
     * @throws IOException
     */
    private JSONObject consultaEmpresas(String token) throws IOException
    {
        JSONObject request = new JSONObject();
        final String codigoARL      = "CodigoARL";
        final String fechaInicio    = "FechaInicio";
        final String fechaFin       = "FechaFin";

        request.put(codigoARL, configFile.getProp(codigoARL));
        request.put(fechaInicio, configFile.getProp(fechaInicio));
        request.put(fechaFin, configFile.getProp(fechaFin));

        HttpPost post = new HttpPost(URL_EMPRESAS);
        post.setHeader(CONTENT_TYPE, CONTENT_TYPE_JSON);
        post.setHeader(AUTHORIZATION, BEARER.concat(token));
        post.setEntity(new ByteArrayEntity(request.toString().getBytes()));
        StringBuilder sb = new StringBuilder();

        try (CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(post))
        {
            int status_code = response.getStatusLine().getStatusCode();
            if(status_code >= 200 && status_code <= 204)
            {
                if(response.getEntity().getContentLength() > 0)
                {
                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(response.getEntity().getContent()), 65728);
                    String line = null;
                    while ((line = reader.readLine()) != null)
                    {
                        sb.append(line);
                    }

                }

                return new JSONObject(sb.toString());
            }else
            {
                throw new IllegalStateException("Error en el consultaEmpresas: ".concat(status_code + ""));
            }
        }
    }

    /**
     *
     * @param token
     * @return
     * @throws IOException
     */
    private JSONObject consultaEstructuraEmpresas(String token) throws IOException
    {

        HttpPost post = new HttpPost(URL_EMPRESAS);
        post.setHeader(CONTENT_TYPE, CONTENT_TYPE_JSON);
        post.setHeader(AUTHORIZATION, BEARER.concat(token));
        StringBuilder sb = new StringBuilder();

        try (CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(post))
        {
            int status_code = response.getStatusLine().getStatusCode();
            if(status_code >= 200 && status_code <= 204)
            {
                if(response.getEntity().getContentLength() > 0)
                {
                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(response.getEntity().getContent()), 65728);
                    String line = null;
                    while ((line = reader.readLine()) != null)
                    {
                        sb.append(line);
                    }

                }

                return new JSONObject(sb.toString());
            }else
            {
                throw new IllegalStateException("Error en el consultaEstructuraEmpresas: ".concat(status_code + ""));
            }
        }
    }
}
