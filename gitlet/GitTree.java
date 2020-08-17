package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/** Tree data structure that stores the commits and their branches.
 * @author Srishti Agarwal
 * reference: Anuj Shah and I created this design structure together. */
@SuppressWarnings("unchecked")
public class GitTree implements Serializable {
    /** Creates Tree of commits to keep track of master and branches. */

    /** Create Empty CommitTree. */
    public GitTree() {
        _children = new ArrayList<>();
    }

    /** @param commit .
     * @param parent .
     * Create CommitTree. */
    public GitTree(Commit commit, GitTree parent) {
        _currentTreeCommit = commit.getRefId();
        _parent = parent;
        _children = new ArrayList<GitTree>();

    }

    /** @param commit . Create CommitTree. */
    public GitTree(Commit commit) {
        _currentTreeCommit = commit.getRefId();
        if (Main.HEADPOINTER.exists()) {
            _parent = find(Main.getGitTree(), Main.getHeadPointerID());
            _parent._children.add(this);
        } else {
            _parent = null;
        }
        _children = new ArrayList<GitTree>();
    }

    /** Add a commit to tree.
     * @param current */
    public void add(Commit current) {
        if (Main.getHeadPointer() != null) {
            String[] head = Main.getHeadPointer();
            find(this,
                    head[0])._children.add(new GitTree(current));
        }
    }

    /** Find a commit in tree.
     * @param tree gitTree.
     * @param id String.
     * @return GitTree . */
    public static GitTree find(GitTree tree, String id) {
        if (tree.getCurrentTreeCommit().equals(id)) {
            return tree;
        } else {
            for (GitTree branch: tree._children) {
                GitTree result = find(branch, id);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }
    }

    /** Find a commit in tree.
     * @param tree .
     * @param id .
     * @return GitTree . */
    public static GitTree findSub(GitTree tree, String id) {
        if (tree.getCurrentTreeCommit().contains(id)) {
            return tree;
        } else {
            for (GitTree branch: tree._children) {
                GitTree result = find(branch, id);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }
    }

    /** put in c ommithash.
     * @param s .
     * @param c .*/
    public static void putCommitHash(String s, Commit c) {
        _commitHash.put(s, c);
    }

    /** @return GitTree getParent. */
    public GitTree getParent() {
        return _parent;
    }

    /** @return String getCurrentTreeCommit. */
    public String getCurrentTreeCommit() {
        return _currentTreeCommit;
    }

    /** @return ArrayList<GitTree> getChildren. */
    public ArrayList<GitTree> getChildren() {
        return _children;
    }

    /** @return HashMap<String, Commit> get commitHash. */
    public static HashMap<String, Commit> getCommitHash() {
        return _commitHash;
    }

    /** @return ArrayList<GitTree> getChildren. */
    private static HashMap<String,
            Commit> _commitHash = new HashMap<String, Commit>();

    /** children. */
    private ArrayList<GitTree> _children;

    /** current commit. */
    private String _currentTreeCommit;

    /** parent of current. */
    private GitTree _parent;
}
