package edu.kit.informatik.test;

import edu.kit.informatik.network.IP;
import edu.kit.informatik.network.Network;
import edu.kit.informatik.util.ParseException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

public class AllTests {
    @Test
    void simpleIP() throws ParseException {
        assertEquals(new IP("0.0.0.0").toString(), "0.0.0.0");
        assertEquals(new IP("255.255.255.255").toString(), "255.255.255.255");
    }

    @Test
    void invalidIP() {
        assertThrows(ParseException.class, () ->
                new IP("256.256.256.256"));
        assertThrows(ParseException.class, () ->
                new IP("00.00.00.001"));
        assertThrows(ParseException.class, () ->
                new IP("255.255.255.255.255"));
        assertThrows(ParseException.class, () ->
                new IP("a.a.a.a"));
        assertThrows(ParseException.class, () ->
                new IP("0.0.0.0."));
        assertThrows(ParseException.class, () ->
                new IP("..."));
    }

    @Test
    void validNetworkCreation() throws ParseException {
        IP root = new IP("141.255.1.133");
        List<List<IP>> levels = List.of(List.of(root),
                List.of(new IP("0.146.197.108"), new IP("122.117.67.158")));
        Network network = new Network(root, levels.get(1));
        assertEquals("(141.255.1.133 0.146.197.108 122.117.67.158)", network.toString(root));

        root = new IP("141.255.1.133");
        network = new Network("(141.255.1.133 (255.255.255.255 0.0.0.0 (45.45.45.45 (34.34.34.34 1.1.1.1))) 255.0.0.234)");
        assertEquals("(141.255.1.133 255.0.0.234 (255.255.255.255 0.0.0.0 (45.45.45.45 (34.34.34.34 1.1.1.1))))", network.toString(root));
    }

    @Test
    void mateosTest() throws ParseException {
        new Network("(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 (39.20.222.120 252.29.23.0 116.132.83.77)))");
    }

    @Test
    void changeRoot() throws ParseException {
        IP root = new IP("141.255.1.133");
        List<List<IP>> levels = List.of(List.of(root),
                List.of(new IP("0.146.197.108"), new IP("122.117.67.158")));
        Network network = new Network(root, levels.get(1));
        assertEquals("(141.255.1.133 0.146.197.108 122.117.67.158)", network.toString(root));
        root = new IP("0.146.197.108");
        assertEquals("(0.146.197.108 (141.255.1.133 122.117.67.158))", network.toString(root));

        root = new IP("141.255.1.133");
        network = new Network("(141.255.1.133 (255.255.255.255 0.0.0.0 (45.45.45.45 (34.34.34.34 1.1.1.1))) 255.0.0.234)");
        assertEquals("(141.255.1.133 255.0.0.234 (255.255.255.255 0.0.0.0 (45.45.45.45 (34.34.34.34 1.1.1.1))))", network.toString(root));
        root = new IP("0.0.0.0");
        assertEquals("(0.0.0.0 (255.255.255.255 (45.45.45.45 (34.34.34.34 1.1.1.1)) (141.255.1.133 255.0.0.234)))", network.toString(root));
    }

    @Test
    void invalidNetworkCreation() {
        assertThrows(ParseException.class, () -> {
            new Network("(244.244.244.244)");
        });

        assertThrows(ParseException.class, () -> {
            new Network("(244.244.244.244 256.266.256.256)");
        });
        assertThrows(ParseException.class, () -> {
            new Network("(244.244.244.244 (1.1.1.1))");
        });
//        TODO: Circular dependency
        assertThrows(ParseException.class, () -> {
            new Network("(244.244.244.244 (1.1.1.1 244.244.244.244 0.0.0.0))");
        });
        assertThrows(ParseException.class, () -> {
            new Network("244.244.244.244 (1.1.1.1 0.0.0.0)");
        });
        assertThrows(ParseException.class, () -> {
            new Network("(244.244.244.244 (1.1.1.1 0.0.0.0)");
        });
    }

    @Test
    void checkRoute() throws ParseException {
        Network network = new Network("(141.255.1.133 (255.255.255.255 0.0.0.0 (45.45.45.45 (34.34.34.34 1.1.1.1))) 255.0.0.234)");
        assertEquals("[255.255.255.255, 45.45.45.45, 34.34.34.34, 1.1.1.1]", network.getRoute(new IP("255.255.255.255"), new IP("1.1.1.1")).toString());
        assertEquals("[0.0.0.0, 255.255.255.255, 141.255.1.133, 255.0.0.234]", network.getRoute(new IP("0.0.0.0"), new IP("255.0.0.234")).toString());
    }

    @Test
    void checkList() throws ParseException {
        Network network = new Network("(141.255.1.133 0.146.197.108 122.117.67.158)");
        assertEquals("[0.146.197.108, 122.117.67.158, 141.255.1.133]", network.list().toString());
        network = new Network("(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 (39.20.222.120 252.29.23.0 116.132.83.77)))");
        assertEquals("[0.146.197.108, 34.49.145.239, 39.20.222.120, 77.135.84.171, 85.193.148.81, 116.132.83.77, 122.117.67.158, 141.255.1.133, 231.189.0.127, 252.29.23.0]", network.list().toString());
        network = new Network("(141.255.1.133 (255.255.255.255 0.0.0.0 (45.45.45.45 (34.34.34.34 1.1.1.1))) 255.0.0.234)");
        assertEquals("[0.0.0.0, 1.1.1.1, 34.34.34.34, 45.45.45.45, 141.255.1.133, 255.0.0.234, 255.255.255.255]", network.list().toString());
    }

    @Test
    void checkHeight() throws ParseException {
        Network network = new Network("(141.255.1.133 0.146.197.108 122.117.67.158)");
        assertEquals(1, network.getHeight(new IP("141.255.1.133")));
        assertEquals(2, network.getHeight(new IP("0.146.197.108")));
        network = new Network("(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 (39.20.222.120 252.29.23.0 116.132.83.77)))");
        assertEquals(3, network.getHeight(new IP("85.193.148.81")));
        assertEquals(4, network.getHeight(new IP("77.135.84.171")));
        network = new Network("(141.255.1.133 (255.255.255.255 0.0.0.0 (45.45.45.45 (34.34.34.34 1.1.1.1))) 255.0.0.234)");
        assertEquals(4, network.getHeight(new IP("141.255.1.133")));
        assertEquals(4, network.getHeight(new IP("34.34.34.34")));
    }
}
