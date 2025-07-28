package com.Hashira.shamir_ss;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.*;

public class Main {

    static class Point {
        int x;
        BigInteger y;
        Point(int x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    public static void main(String[] args) throws Exception {
        String[] testcases = {"testcase1.json", "testcase2.json"};

        for (String fileName : testcases) {
            List<Point> points = new ArrayList<>();
            InputStream is = Main.class.getClassLoader().getResourceAsStream(fileName);
            if (is == null) throw new RuntimeException("File not found: " + fileName);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(is);

            int k = root.get("keys").get("k").asInt();

            Iterator<String> fieldNames = root.fieldNames();
            while (fieldNames.hasNext()) {
                String key = fieldNames.next();
                if (key.equals("keys")) continue;

                int x = Integer.parseInt(key);
                JsonNode node = root.get(key);
                int base = Integer.parseInt(node.get("base").asText());
                String value = node.get("value").asText();
                BigInteger y = new BigInteger(value, base);

                points.add(new Point(x, y));
            }

            // Sort and select first k points
            points.sort(Comparator.comparingInt(p -> p.x));
            List<Point> selected = points.subList(0, k);

            BigInteger secret = lagrangeInterpolation(BigInteger.ZERO, selected);
            System.out.println("Secret from " + fileName + ": " + secret);
        }
    }

    
    private static BigInteger lagrangeInterpolation(BigInteger x, List<Point> points) {
        BigInteger result = BigInteger.ZERO;

        for (int i = 0; i < points.size(); i++) {
            BigInteger xi = BigInteger.valueOf(points.get(i).x);
            BigInteger yi = points.get(i).y;

            BigInteger term = yi;
            for (int j = 0; j < points.size(); j++) {
                if (i == j) continue;
                BigInteger xj = BigInteger.valueOf(points.get(j).x);

                term = term.multiply(x.subtract(xj))
                           .divide(xi.subtract(xj));
            }

            result = result.add(term);
        }

        return result;
    }
}