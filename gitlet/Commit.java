
package gitlet;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;



/** Commit class stores all commits and its Meta Data.
 * @author Srishti Agarwal
 * reference: Anuj Shah and I created this design structure together. */
@SuppressWarnings("unchecked")
public class Commit implements Serializable {

    /** Construct empty initial Commit. */
    public Commit() {
        _fileNameHash = new HashMap<>();
        _timeStamp = "Wed Dec 31 16:00:00 1969 -0800";
        _parents = null;
        _logMessage = "initial commit";
        _refID = "0";
        _branch = "master";

    }

    /** Construct non-empty Commit.
     * @param message log message. */
    public Commit(String message) {
        SimpleDateFormat c = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        if (!Main.getForAddition().entrySet().isEmpty()
                || !Main.getForRemoval().entrySet().isEmpty()) {
            if (message.equals("")) {
                Main.exit("Please enter a commit message.");
            }
            _logMessage = message;
            _timeStamp = c.format(new Date());
            _fileNameHash = (HashMap<String, String>) (Main
                    .getCurrentCommit()._fileNameHash.clone());
            Set<Map.Entry<String, Boolean>> tracker
                    = Main.getTrackerHash().entrySet();
            for (Map.Entry<String, Boolean> i
                    : tracker) {
                if (!i.getValue()) {
                    _fileNameHash.remove(i.getKey());
                }
            }
            for (Map.Entry<String, String> key
                    : Main.getForAddition().entrySet()) {
                _fileNameHash.put(key.getKey(), key.getValue());
                Main.getTrackerHash().put(key.getKey(), true);
            }
            _parentID = Main.getCurrentCommit()._refID;
            _branch = Main.getHeadPointerBranch();
        } else {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
    }

    /** @return Timestamp . */
    public String getTimeStamp() {
        return _timeStamp;
    }

    /** @return String . */
    public String getLogMessage() {
        return _logMessage;
    }

    /** @return String . */
    public String getRefId() {
        return _refID;
    }

    /** @return String . */
    public String getparentID() {
        return _parentID;
    }

    /** @return Hashmap<String, String> . */
    public HashMap<String, String> getFileNameHash() {
        return _fileNameHash;
    }

    /** @param id String . */
    public void setRefID(String id) {
        _refID = id;
    }

    /** @return String Branch . */
    public String getBranch() {
        return _branch;
    }

    /** Commit sha1. */
    private String _refID;

    /** Parent commit sha1. */
    private String _parentID;

    /** List of parent commits sha1. */
    private ArrayList<String> _parents = new ArrayList<>();

    /** Timestamop of this commit. */
    private String _timeStamp;

    /** Log Message of this commit. */
    private String _logMessage;

    /** Branch name of this commit. */
    private String _branch;

    /** All files to blobs in this commit. */
    private HashMap<String, String>
            _fileNameHash = new HashMap<String, String>();
}
