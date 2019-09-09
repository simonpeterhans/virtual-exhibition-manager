package ch.unibas.dmi.dbis.vrem.server.handlers.collection;

import ch.unibas.dmi.dbis.vrem.database.dao.VREMWriter;
import ch.unibas.dmi.dbis.vrem.model.collection.ExhibitUpload;
import ch.unibas.dmi.dbis.vrem.server.handlers.basic.ParsingActionHandler;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;

public class UploadExhibitHandler extends ParsingActionHandler<ExhibitUpload> {

    private final VREMWriter writer;
    private final Path docRoot;

    public UploadExhibitHandler(VREMWriter writer, Path docRoot) {
        this.writer = writer;
        this.docRoot = docRoot;
    }

    @Override
    public ExhibitUpload doPost(ExhibitUpload exhibitUpload, Map<String, String> parameters) {

        String path = this.writer.uploadExhibit(exhibitUpload);

        // Save file to disk.
        String base64Image = exhibitUpload.file.split(",")[1];
        byte[] decodedImage = Base64.getDecoder().decode(base64Image.getBytes(StandardCharsets.UTF_8));

        // Create directory for ArtCollection if it doesn't already exist.
        File dir = new File(docRoot + "/" + exhibitUpload.artCollection);
        if (!dir.exists()) {
            dir.mkdir();
        }

        // Save the file to the directory.
        try {
            FileOutputStream fos = new FileOutputStream(docRoot + "/" + path);
            fos.write(decodedImage);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return exhibitUpload;
    }

    @Override
    public Class<ExhibitUpload> inClass() {
        return ExhibitUpload.class;
    }
}
