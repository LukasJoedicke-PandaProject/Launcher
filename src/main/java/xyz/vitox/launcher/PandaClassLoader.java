package xyz.vitox.launcher;

import xyz.vitox.Main;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.*;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class PandaClassLoader {

    /**
     * Starting the actual program from Memory
     */
    public void startPanda() {
        try {
            setURLScheme();
            loadJarFromURL(new URL("pandaprot:fakeparameter"));
            final Object[] argz = new Object[1];
            argz[0] = new String[] { "panda_start", Main.LICENSE_KEY};
            ClassLoader.getSystemClassLoader().loadClass("xyz.vitox.discordtool.Main").getMethod("main", String[].class).invoke(null, argz);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Setting a custom URL Scheme to be able to load the .jar into the SystemClassloader
     */
    public void setURLScheme() {
        URL.setURLStreamHandlerFactory(urlProtocol -> {
            if ("pandaprot".equalsIgnoreCase(urlProtocol)) {
                return new URLStreamHandler() {
                    @Override
                    protected URLConnection openConnection(URL url) throws IOException {
                        return new URLConnection(url) {
                            public void connect() throws IOException { }
                            public InputStream getInputStream() throws IOException {
                                return new ByteArrayInputStream(getBytesFromUrl("https://localhost.com/files/layer.jar" + DeviceUtil.getFileQuery()));
                            }
                        };
                    }
                };
            }
            return null;
        });
    }

    /**
     * Loading the jar into the Classloader with our custom URL Scheme
     * @param jarURL
     * @throws Exception
     */
    public void loadJarFromURL(URL jarURL) throws Exception {
        URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Method systemClassloaderMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        systemClassloaderMethod.setAccessible(true);
        systemClassloaderMethod.invoke(classLoader, jarURL);
        classLoader.findResource("/resource-404");
    }

    /**
     * Downloading the actual Program
     * @param urlText
     * @return
     * @throws IOException
     */
    public byte[] getBytesFromUrl(String urlText) throws IOException {
        URL url = new URL(urlText);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        int totalFileSize = conn.getContentLength();
        conn.setRequestProperty("User-Agent", "PandaLauncher");

        try (InputStream inputStream = conn.getInputStream()) {
            int n = 0;
            int totalProccessed = 0;
            byte [] buffer = new byte[ 1024 ];
            while (-1 != (n = inputStream.read(buffer))) {
                totalProccessed += n;
                printProgress(System.currentTimeMillis(), totalFileSize, totalProccessed);
                output.write(buffer, 0, n);
            }
            System.out.println("");
            System.out.println("");
        }

        return output.toByteArray();
    }

    /**
     * Printing the progress in a progressbar to the console.
     * @param startTime
     * @param total
     * @param current
     */
    public static void printProgress(long startTime, long total, long current) {
        long eta = current == 0 ? 0 :
                (total - current) * (System.currentTimeMillis() - startTime) / current;

        String etaHms = current == 0 ? "N/A" :
                String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(eta),
                        TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1));

        StringBuilder string = new StringBuilder(140);
        int percent = (int) (current * 100 / total);
        string
                .append('\r')
                .append(String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), "")))
                .append(String.format("%d%% [", percent))
                .append(String.join("", Collections.nCopies(percent, "=")))
                .append('>')
                .append(String.join("", Collections.nCopies(100 - percent, " ")))
                .append(']')
                .append(String.join("", Collections.nCopies(current == 0 ? (int) (Math.log10(total)) : (int) (Math.log10(total)) - (int) (Math.log10(current)), " ")));

        System.out.print(string);
    }
}
