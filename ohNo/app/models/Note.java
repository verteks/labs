package models;

/**
 *
 */
import java.util.*;
import play.data.validation.Constraints.*;
import javax.persistence.*;
import play.db.ebean.Model;

@Entity
public class Note extends Model {

    @Id
    public Long id;
    @Required
    public String name;
    public String cellPhone;
    public  String homePhone;

    public static Finder<Long, Note> find = new Finder<Long, Note>(
            Long.class, Note.class
    );

    public static List<Note> all() {
        return find.all();
    }

    @Deprecated
    public static void edit(Note note,Long id) {
        note.update(id);
    }

    public static Note find(Long id) {
        return find.ref(id);
    }
    public static void delete(Long id) {

        find.ref(id).delete();
    }
    public static void create(Note note) {
        note.save();
    }
    public static void refresh(Note note) {
        note.refresh();
    }
}