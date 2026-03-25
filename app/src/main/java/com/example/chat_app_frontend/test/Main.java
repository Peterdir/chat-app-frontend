package test;
import io.agora.rtc2.RtcEngine;
import java.lang.reflect.Method;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting...");
        for (Method m : RtcEngine.class.getDeclaredMethods()) {
            if (m.getName().toLowerCase().contains("screen")) {
                System.out.println(m.getName() + " -> " + m.getParameterTypes().length + " args");
                for (Class<?> p : m.getParameterTypes()) {
                    System.out.println("  Arg: " + p.getName());
                }
            }
        }
    }
}
