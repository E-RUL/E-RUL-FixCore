import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Offline, single-annotation patch. Never writes the input mod jar. */
public final class PatchTenkeiAe2 {
    public static final String ENTRY = "com/tenkeitech/mixin/ae2/CraftingCPUClusterMixin.class";
    static String hash(byte[] b) throws Exception {
        StringBuilder s = new StringBuilder();
        for (byte v : MessageDigest.getInstance("SHA-256").digest(b)) s.append(String.format("%02X", v & 255));
        return s.toString();
    }
    public static void main(String[] args) throws Exception {
        byte[] before;
        try (ZipFile z = new ZipFile(args[0])) {
            ZipEntry e = z.getEntry(ENTRY);
            if (e == null) throw new IllegalStateException("Target class absent");
            try (java.io.InputStream in = z.getInputStream(e)) { before = in.readAllBytes(); }
        }
        ClassNode node = new ClassNode();
        new ClassReader(before).accept(node, 0);
        int changed = 0;
        for (MethodNode m : node.methods) {
            if (!m.name.equals("tenkeitech$pushPatternBatch")) continue;
            if (m.visibleAnnotations == null) continue;
            for (AnnotationNode a : m.visibleAnnotations) {
                if (!a.desc.equals("Lorg/spongepowered/asm/mixin/injection/Redirect;")) continue;
                boolean exactTarget = false;
                for (int i=0; i<a.values.size(); i+=2) {
                    if (a.values.get(i).equals("at")) {
                        AnnotationNode at = (AnnotationNode)a.values.get(i+1);
                        for (int k=0; k<at.values.size(); k+=2) {
                            if (at.values.get(k).equals("target") && at.values.get(k+1).equals("Lappeng/api/networking/crafting/ICraftingMedium;pushPattern(Lappeng/api/networking/crafting/ICraftingPatternDetails;Lnet/minecraft/inventory/InventoryCrafting;)Z")) exactTarget = true;
                        }
                    }
                }
                if (!exactTarget) throw new IllegalStateException("Unexpected redirect target");
                for (int i=0; i<a.values.size(); i+=2) {
                    if (a.values.get(i).equals("require")) {
                        if (!Integer.valueOf(1).equals(a.values.get(i+1))) throw new IllegalStateException("Expected explicit require=1");
                        a.values.set(i+1, 0); changed++;
                    }
                }
            }
        }
        if (changed != 1) throw new IllegalStateException("Expected exactly one annotation change, got " + changed);
        ClassWriter writer = new ClassWriter(0); node.accept(writer);
        byte[] after = writer.toByteArray();
        // Verify semantic isolation: restoring this annotation must reconstruct the same ASM tree bytes.
        ClassNode verify = new ClassNode(); new ClassReader(after).accept(verify, 0);
        for (MethodNode m : verify.methods) if (m.name.equals("tenkeitech$pushPatternBatch"))
            for (AnnotationNode a : m.visibleAnnotations) if (a.desc.endsWith("/Redirect;"))
                for (int i=0;i<a.values.size();i+=2) if (a.values.get(i).equals("require")) a.values.set(i+1,1);
        ClassWriter restored = new ClassWriter(0); verify.accept(restored);
        ClassNode original = new ClassNode(); new ClassReader(before).accept(original,0);
        ClassWriter normalized = new ClassWriter(0); original.accept(normalized);
        if (!Arrays.equals(restored.toByteArray(), normalized.toByteArray())) throw new IllegalStateException("Unexpected semantic changes");
        Path out = Paths.get(args[1]); Path cls = out.resolve("classes").resolve(ENTRY);
        Files.createDirectories(cls.getParent()); Files.write(cls, after);
        String json = "{\n  \"entry\": \""+ENTRY+"\",\n  \"originalSha256\": \""+hash(before)+"\",\n  \"patchedSha256\": \""+hash(after)+"\",\n  \"change\": \"tenkeitech$pushPatternBatch @Redirect require 1 -> 0\"\n}\n";
        Files.writeString(out.resolve("manifest.json"),json);
        System.out.println("PASS: exactly one annotation changed; code, fields, other annotations unchanged");
        System.out.println(json);
    }
}
