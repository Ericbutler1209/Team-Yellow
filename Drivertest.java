import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DriverTest {

    // --- tiny helper assertion ---
    static void assertApprox(String name, double actual, double expected, double tol) {
        if (Math.abs(actual - expected) > tol) {
            System.out.printf("FAIL: %s  expected=%.4f actual=%.4f%n", name, expected, actual);
        } else {
            System.out.printf("PASS: %s%n", name);
        }
    }

    static void assertEquals(String name, Object actual, Object expected) {
        if (!Objects.equals(actual, expected)) {
            System.out.printf("FAIL: %s  expected=%s actual=%s%n", name, expected, actual);
        } else {
            System.out.printf("PASS: %s%n", name);
        }
    }

    // Create a temp CSV so tests don't rely on big real file
    static Path writeMiniCsv() throws IOException {
        String csv =
            "age,sex,bmi,children,smoker,region,charges\n" +
            "18,female,20.0,0,no,southwest,2000.00\n" +
            "30,male,30.0,2,yes,northwest,30000.00\n" +
            "45,female,25.0,1,no,southeast,15000.00\n";
        Path p = Paths.get("mini_insurance.csv");
        Files.writeString(p, csv);
        return p;
    }

    public static void main(String[] args) throws Exception {
        // Arrange
        Path csv = writeMiniCsv();

        // Act
        java.util.List<Driver.InsuranceRecord> recs = Driver.loadFirstN(csv.toString(), 3);

        // Assert basic load
        assertEquals("count loaded", recs.size(), 3);
        assertEquals("first.age", recs.get(0).age, 18);
        assertEquals("second.children", recs.get(1).children, 2);

        // Feature 02 stats
        java.util.Map<String, Driver.Stats> stats = Driver.computeFeature02Stats(recs);
        Driver.Stats age = stats.get("age");
        Driver.Stats bmi = stats.get("bmi");
        Driver.Stats children = stats.get("children");
        Driver.Stats charges = stats.get("charges");

        assertEquals("age.count", age.count, 3L);
        assertApprox("age.min",   age.min,   18.0, 1e-9);
        assertApprox("age.max",   age.max,   45.0, 1e-9);
        assertApprox("age.avg",   age.avg(), (18+30+45)/3.0, 1e-9);

        assertEquals("children.count", children.count, 3L);
        assertApprox("children.sum",   children.sum,   0+2+1, 1e-9);

        assertEquals("bmi.count", bmi.count, 3L);
        assertApprox("bmi.avg",   bmi.avg(), (20.0+30.0+25.0)/3.0, 1e-9);

        assertEquals("charges.count", charges.count, 3L);
        assertApprox("charges.max",   charges.max,   30000.0, 1e-9);

        // childrenCounts()
        java.util.Map<Integer,Integer> cc = Driver.childrenCounts(recs);
        assertEquals("children=0", cc.get(0), 1);
        assertEquals("children=1", cc.get(1), 1);
        assertEquals("children=2", cc.get(2), 1);

        System.out.println("All tests done.");
    }
}
