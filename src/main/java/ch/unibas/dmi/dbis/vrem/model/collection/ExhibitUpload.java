package ch.unibas.dmi.dbis.vrem.model.collection;

import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibit;

public class ExhibitUpload {

    public String artCollection;
    public Exhibit exhibit;
    public String file;
    public String fileExtension;

    public ExhibitUpload(String artCollection, Exhibit exhibit, String file, String fileExtension) {
        this.artCollection = artCollection;
        this.exhibit = exhibit;
        this.file = file;
        this.fileExtension = fileExtension;
    }
}
