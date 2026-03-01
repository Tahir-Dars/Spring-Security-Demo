package bank.springdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@SpringBootApplication
public class SpringDemoApplication {

    public static void main(String[] args) {
        killProcessOnPort(8080);
        SpringApplication.run(SpringDemoApplication.class, args);
    }

    private static void killProcessOnPort(int port) {
        try {
            Process process = new ProcessBuilder("cmd.exe", "/c",
                    "for /f \"tokens=5\" %a in ('netstat -ano ^| findstr :" + port + " ^| findstr LISTENING') do taskkill /PID %a /F")
                    .redirectErrorStream(true).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[Port Cleanup] " + line);
            }
            process.waitFor();
            System.out.println("Port " + port + " cleanup attempted.");
        } catch (Exception e) {
            System.out.println("Could not kill process on port " + port + ": " + e.getMessage());
        }
    }

}
