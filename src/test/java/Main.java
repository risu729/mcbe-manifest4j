import io.github.risu729.mcbe.manifest4j.*;

public class Main {
  public static void main(String[] args) {
    Manifest m = ManifestTemplates.get(Module_.Type.RESOURCES, true);
    System.out.println(m.toString());
    System.out.println(m.toJson());
  }
}