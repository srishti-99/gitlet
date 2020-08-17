package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Collections;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Srishti Agarwal
 * reference: Anuj's idea to make everything a file instead of data structures.
 */
@SuppressWarnings("unchecked")

public class Main {
    /** Current Working Directory file path. */
    static final File CWD = new File(".");
    /** Gitlet directory file path.*/
    static final File GITLET = Utils.join(CWD, ".gitlet");
    /** Tree of commits. */
    static final File GITTREE = Utils.join(GITLET, "gitTree");
    /** Reference ID of headpointer which points to the latest commit. */
    static final File HEADPOINTER = Utils.join(GITLET, "headPointer");
    /** Stores a hashmap from commit reference ID to the commit object. */
    static final File COMMITHASH = Utils.join(GITLET, "commitHash");
    /** stagingArea directory file path. */
    static final File STAGINGAREA = Utils.join(GITLET, "stagingArea");
    /** Staging area --> forAddition directory file path. */
    static final File FORADDITION = Utils.join(STAGINGAREA, "forAddition");
    /** Staging area --> forRemoval directory file path. */
    static final File FORREMOVAL = Utils.join(STAGINGAREA, "forRemoval");
    /** Stores a hashmap that records if files are being tracked. */
    static final File TRACKERHASH = Utils.join(GITLET, "trackerHash");
    /** Stores a hashmap from blob reference ID to the contents of the files.*/
    static final File BLOBHASH = Utils.join(GITLET, "blobHash");
    /** Stores a hashmap from branch name to branch pointer (commit).*/
    static final File BRANCHMAP = Utils.join(GITLET, "branchMap");
    /** length of sha1 ref id. */
    static final int REFIDLEN = 40;


    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args[0].equals("init")) {
            if (args.length == 1) {
                init();
            } else {
                exit("Incorrect number of arguments");
            }
        } else if (args[0].equals("add")) {
            if (args.length == 2) {
                add(args[1]);
            } else {
                exit("Incorrect number of arguments");
            }
        } else if (args[0].equals("commit")) {
            if (args.length == 2) {
                commit(args[1]);
            } else {
                exit("Incorrect number of arguments");
            }
        } else if (args[0].equals("log")) {
            if (args.length == 1) {
                log();
            } else {
                exit("Incorrect number of arguments");
            }
        } else if (args[0].equals("rm")) {
            if (args.length == 2) {
                rm(args[1]);
            } else {
                exit("Incorrect number of arguments");
            }
        } else if (args[0].equals("global-log")) {
            if (args.length == 1) {
                globalLog();
            } else {
                exit("Incorrect number of arguments");
            }
        } else if (args[0].equals("find")) {
            if (args.length == 2) {
                find(args[1]);
            } else {
                exit("Incorrect number of arguments");
            }
        } else {
            main2(args);
        }
    }

    /** @param args ...
     *  continued main function. */
    public static void main2(String... args) {
        if (args[0].equals("branch")) {
            if (args.length == 2) {
                branch(args[1]);
            } else {
                exit("Incorrect number of arguments");
            }
        } else if (args[0].equals("checkout")) {
            if (args.length == 2) {
                checkout(args[1]);
            } else if (args.length == 3) {
                if (args[1].equals("--")) {
                    checkout(args[1], args[2]);
                } else {
                    exit("Incorrect operands.");
                }
            } else if (args.length == 4) {
                if (args[2].equals("--")) {
                    checkout(args[1], args[2], args[3]);
                } else {
                    exit("Incorrect operands.");
                }
            } else {
                exit("Incorrect number of arguments");
            }
        } else if (args[0].equals("status")) {
            if (args.length == 1) {
                status();
            } else {
                exit("Incorrect number of arguments");
            }
        } else if (args[0].equals("rm-branch")) {
            if (args.length == 2) {
                rmbranch(args[1]);
            } else {
                exit("Incorrect number of arguments");
            }
        } else if (args[0].equals("reset")) {
            if (args.length == 2) {
                reset(args[1]);
            } else {
                exit("Incorrect number of arguments");
            }
        }
    }

    /** exiting with 0 code and.
     * @param message . */
    public static void exit(String message) {
        System.out.println(message);
        System.exit(0);
    }

    /** Initialize a .gitlet directory. */
    public static void init() {
        if (!GITLET.exists()) {
            GITLET.mkdir();
            STAGINGAREA.mkdir();
            Commit firstCommit = new Commit();
            GitTree commitTree = new GitTree(firstCommit);
            HashMap<String, Commit> commits = new HashMap<String, Commit>();
            commits.put("0", firstCommit);
            HashMap<String, byte[]> blobs
                    = new HashMap<String, byte[]>();
            HashMap<String, String> stage
                    = new HashMap<String, String>();
            HashMap<String, Boolean> tracker
                    = new HashMap<String, Boolean>();
            HashMap<String, Commit> branches
                    = new HashMap<String, Commit>();
            String[] pointer = {firstCommit.getRefId(), "master"};
            Utils.writeObject(HEADPOINTER, pointer);
            branches.put("master", firstCommit);
            Utils.writeObject(BRANCHMAP, branches);
            Utils.writeObject(GITTREE, commitTree);
            Utils.writeObject(COMMITHASH, commits);
            Utils.writeObject(BLOBHASH, blobs);
            Utils.writeObject(FORADDITION, stage);
            Utils.writeObject(FORREMOVAL, stage);
            Utils.writeObject(TRACKERHASH, tracker);
        } else {
            System.out.println(
                    "A Gitlet version-control "
                            + "system already exists in the current directory."
                            + "");
            System.exit(0);
        }
    }

    /** @param file String.
     * Add files to the staging area for addition. */
    public static void add(String file) {
        File working = Utils.join(CWD, file);
        HashMap<String, byte[]> blobs = getBlobHash();
        HashMap<String, String> stage = getForAddition();
        Commit current = getCurrentCommit();

        if (!working.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        } else {
            HashMap<String, String> stageRem = getForRemoval();
            String removed = stageRem.remove(file);
            Utils.writeObject(FORREMOVAL, stageRem);
            if (removed == null) {
                byte[] temp = Utils.readContents(working);
                String id = Utils.sha1(temp);
                if (blobs != null) {
                    setBlobHash(id, temp);
                }
                if (current.getFileNameHash().get(file) == null
                        || !current.getFileNameHash().get(file).equals(id)) {
                    setForAddition(file, id);
                    setTrackerHash(file, true);
                }
                if (stage.get(file) != null) {
                    stage.remove(file);
                    Utils.writeObject(FORADDITION, stage);
                }
            }
        }
    }

    /** Commit command.
     * @param message .
     * */
    public static void commit(String message) {
        Commit current = new Commit(message);
        String id = Utils.sha1(Utils.serialize(current));
        current.setRefID(id);
        setGitTree(current);
        setCommitHash(id, current);
        setHeadPointerID(id);
        setBranchMap(getHeadPointerBranch(), current);
        clearStagingArea();
    }

    /** Generates and prints log. */
    public static void log() {
        GitTree tree = getGitTree();
        GitTree pointer = GitTree.find(tree, getHeadPointerID());
        while (pointer != null) {
            printNode(pointer);
            pointer = pointer.getParent();
        }
    }

    /** Generates and prints global log. */
    public static void globalLog() {
        printAll();
    }

    /** @param file String. */
    public static void rm(String file) {
        HashMap<String, String> stage = getForAddition();
        String removed = stage.remove(file);
        Utils.writeObject(FORADDITION, stage);
        if (removed == null) {
            if (Utils.join(CWD, file).exists()) {
                setForRemoval(file, Utils.sha1(
                        Utils.readContents(Utils.join(CWD, file))));
            }
            if (getCurrentCommit().getFileNameHash().get(file) != null) {
                setTrackerHash(file, false);
                if (Utils.join(CWD, file).exists()) {
                    Utils.join(CWD, file).delete();
                }
            } else {
                if (removed == null) {
                    System.out.println("No reason to remove the file.");
                    System.exit(0);
                }
            }
        }
    }

    /** @param message for log. find command. */
    public static void find(String message) {
        boolean checker = false;
        HashMap<String, Commit> commits = getCommitHash();
        for (Map.Entry<String, Commit> commit : commits.entrySet()) {
            if (commit.getValue().getLogMessage().equals(message)) {
                System.out.println(commit.getKey());
                checker = true;
            }
        }
        if (!checker) {
            exit("Found no commit with that message.");
        }
    }

    /** Status of current commit. */
    public static void status() {
        ArrayList<String> branches = new ArrayList<>();
        ArrayList<String> stagedFiles = new ArrayList<>();
        ArrayList<String> removedFiles = new ArrayList<>();
        ArrayList<String> trackedFiles = new ArrayList<>();
        ArrayList<String> modifiedFiles;
        HashMap<String, String> stagedForRemoval = getForRemoval();
        HashMap<String, String> stagedForAddition = getForAddition();
        HashMap<String, Boolean> tracker = getTrackerHash();
        List<String> all = Utils.plainFilenamesIn(CWD);
        for (String file : all) {
            if (!file.equals(".DS_Store") && !file.equals(".gitignore")
                    && !file.equals("Makefile") && !file.equals("proj3.iml")) {
                if (!tracker.containsKey(file)) {
                    trackedFiles.add(file);
                }
            }
        }
        for (Map.Entry<String, Commit> branch: getBranchMap().entrySet()) {
            if (branch.getKey().equals(getHeadPointerBranch())) {
                branches.add("*" + branch.getKey());
            } else {
                branches.add(branch.getKey());
            }
        }
        for (Map.Entry<String, String> staged: stagedForAddition.entrySet()) {
            stagedFiles.add(staged.getKey());
        }
        for (Map.Entry<String, String> staged: stagedForRemoval.entrySet()) {
            removedFiles.add(staged.getKey());
        }
        modifiedFiles = modified();
        System.out.println("=== Branches ===");
        printlist(branches);
        System.out.println();
        System.out.println("=== Staged Files ===");
        printlist(stagedFiles);
        System.out.println();
        System.out.println("=== Removed Files ===");
        printlist(removedFiles);
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        printlist(modifiedFiles);
        System.out.println();
        System.out.println("=== Untracked Files ===");
        printlist(trackedFiles);
        System.out.println();
    }

    /** @return ArrayList<String> of modified files */
    public static ArrayList<String> modified() {
        HashMap<String, String> stagedForRemoval = getForRemoval();
        HashMap<String, String> stagedForAddition = getForAddition();
        HashMap<String, Boolean> tracker = getTrackerHash();
        HashMap<String, String> currentFileHash
                = getCurrentCommit().getFileNameHash();
        HashMap<String, byte[]> blobs = getBlobHash();
        ArrayList<String> modifiedFiles = new ArrayList<>();
        for (Map.Entry<String, Boolean> t: tracker.entrySet()) {
            boolean stagedAdd = stagedForAddition.containsKey(t.getKey());
            boolean stagedRem = stagedForRemoval.containsKey(t.getKey());
            boolean trackedInCurrent = t.getValue();
            boolean existsInCWD = Utils.join(CWD, t.getKey()).exists();
            String contentsInWorkingDirectory;
            if (trackedInCurrent && existsInCWD && !stagedAdd
                    && currentFileHash.containsKey(t.getKey())) {
                contentsInWorkingDirectory =
                        Utils.readContentsAsString(Utils.join(CWD, t.getKey()));
                String contentsInCommit =
                        new String(blobs.get(currentFileHash.get(t.getKey())));
                if (!contentsInWorkingDirectory.equals(contentsInCommit)) {
                    modifiedFiles.add(t.getKey() + " (modified)");
                }
            }
            if (existsInCWD && stagedAdd) {
                contentsInWorkingDirectory =
                        Utils.readContentsAsString(Utils.join(CWD, t.getKey()));
                String contentsInStaged =
                        new String(blobs.get(
                                stagedForAddition.get(t.getKey())));
                if (!contentsInStaged.equals(contentsInWorkingDirectory)) {
                    modifiedFiles.add(t.getKey() + " (modified)");
                }
            }
            if (stagedAdd && !existsInCWD) {
                modifiedFiles.add(t.getKey() + " (deleted)");
            }
            if (!stagedRem && trackedInCurrent && !existsInCWD) {
                modifiedFiles.add(t.getKey() + " (deleted)");
            }
        }
        return modifiedFiles;
    }

    /** @param l prints in lexicographic order*/
    public static void printlist(ArrayList<String> l) {
        Collections.sort(l);
        for (int i = 0; i < l.size(); i++) {
            System.out.println(l.get(i));
        }
    }

    /** @param branch String. Checkout to branch. */
    public static void checkout(String branch) {
        Commit branchHead = getBranchMap().get(branch);
        if (branchHead == null) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (getHeadPointerBranch().equals(branch)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        List<String> all = Utils.plainFilenamesIn(CWD);
        String headp = getHeadPointerID();
        Commit head = getCommitHash().get(GitTree.find
                (getGitTree(), headp).getCurrentTreeCommit());
        HashMap<String, String> currentFileHash = head.getFileNameHash();
        HashMap<String, String> branchFileHash = branchHead.getFileNameHash();
        HashMap<String, byte[]> blobs = getBlobHash();

        for (String file : all) {
            if (!file.equals(".DS_Store") && !file.equals(".gitignore")
                    && !file.equals("Makefile") && !file.equals("proj3.iml")) {
                if (!currentFileHash.containsKey(file)) {
                    System.out.println("There is an untracked file in the way;"
                            + "delete it or add it first.");
                    System.exit(0);
                } else {
                    if (!branchFileHash.containsKey(file)) {
                        Utils.join(CWD, file).delete();
                    }
                }
            }
        }
        for (Map.Entry<String, String> file
                : branchFileHash.entrySet()) {
            byte[] contents = blobs.get(file.getValue());
            File f = Utils.join(CWD, file.getKey());
            Utils.writeContents(f, contents);
        }
        setHeadPointerBranch(branch);
        setHeadPointerID(branchHead.getRefId());
        clearStagingArea();
    }

    /** @param dash .
     *  @param filename String.
     *  checkout to file in lat commit. */
    public static void checkout(String dash, String filename) {
        checkout(getHeadPointerID(), dash, filename);
    }

    /** @param dash .
     *  @param filename String.
     *  @param commitId latest.
     *  Checkout to file in lat commit. */
    public static void checkout(String commitId, String dash, String filename) {
        GitTree headTree;
        if (commitId.length() == REFIDLEN) {
            headTree = GitTree.find(getGitTree(), commitId);
        } else {
            headTree = GitTree.findSub(getGitTree(), commitId);
        }
        if (headTree == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit head = getCommitHash().get(headTree.getCurrentTreeCommit());
        String blob = head.getFileNameHash().get(filename);
        if (blob == null) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        byte[] contents = getBlobHash().get(blob);
        File file = Utils.join(CWD, filename);
        Utils.writeContents(file, contents);
    }

    /** @param commitId to reset to. */
    public static void reset(String commitId) {
        HashMap<String, Boolean> tracker = getTrackerHash();
        Commit setCommit = null;
        HashMap<String, Commit> commits = getCommitHash();
        if (commitId.length() == REFIDLEN) {
            setCommit = commits.get(commitId);
        } else {
            for (Map.Entry<String, Commit> c: commits.entrySet()) {
                if (c.getKey().contains(commitId)) {
                    setCommit = c.getValue();
                }
            }
        }
        if (setCommit == null) {
            exit("No commit with that id exists.");
        }
        Commit currentCommit = getCurrentCommit();
        HashMap<String, String> currentFileHash
                = currentCommit.getFileNameHash();
        HashMap<String, String> setFileHash
                = setCommit.getFileNameHash();
        List<String> all = Utils.plainFilenamesIn(CWD);
        setHeadPointerID(setCommit.getRefId());
        setHeadPointerBranch(setCommit.getBranch());
        for (Map.Entry<String, Boolean> t: tracker.entrySet()) {
            if (t.getValue() && setFileHash.containsKey(t.getKey())) {
                checkout(commitId, "--", t.getKey());
            }
        }
        for (String file : all) {
            if (!file.equals(".DS_Store") && !file.equals(".gitignore")
                    && !file.equals("Makefile") && !file.equals("proj3.iml")) {
                if (!currentFileHash.containsKey(file)) {
                    System.out.println("There is an untracked file in the way;"
                            + "delete it or add it first.");
                    System.exit(0);
                } else {
                    if (!setFileHash.containsKey(file)) {
                        Utils.join(CWD, file).delete();
                    }
                }
            }
        }
        setHeadPointerID(commitId);
        GitTree currentTree
                = GitTree.find(getGitTree(), currentCommit.getRefId());
        while (!currentTree.getCurrentTreeCommit().
                equals(getCurrentCommit().getRefId())) {
            GitTree parent = currentTree.getParent();
            parent.getChildren().remove(currentTree);
            currentTree = parent;
        }
        setBranchMap(setCommit.getBranch(), setCommit);
        clearStagingArea();
    }

    /** @param name of branch to create*/
    public static void branch(String name) {
        HashMap<String, Commit> branchPointersMap = getBranchMap();
        if (branchPointersMap.get(name) != null) {
            exit("A branch with that name already exists.");
        } else {
            setBranchMap(name, getCurrentCommit());
        }
    }

    /** @param name . */
    public static void rmbranch(String name) {
        if (name.equals(getHeadPointerBranch())) {
            exit("Cannot remove the current branch.");
        }
        HashMap<String, Commit> branch = getBranchMap();
        Commit removed = branch.remove(name);
        Utils.writeObject(BRANCHMAP, branch);
        if (removed == null) {
            exit("A branch with that name does not exist.");
        }
    }

    /**
     * Finds split point of.
     * @param givenBranch   .
     * @param currentBranch .
     * @return GitTree .
     */
    public static GitTree findSplitPoint(String currentBranch,
                                         String givenBranch) {
        HashMap<String, Commit> branchMap = getBranchMap();
        Commit currentPointer = branchMap.get(currentBranch);
        Commit givenPointer = branchMap.get(givenBranch);
        GitTree tree = getGitTree();
        GitTree currentTree = GitTree.find(tree, currentPointer.getRefId());
        GitTree givenTree = GitTree.find(tree, givenPointer.getRefId());
        GitTree parent = currentTree.getParent();
        GitTree splitPoint = null;
        while (parent != null) {
            if (parent.getChildren().contains(givenTree)) {
                splitPoint = parent;
            } else {
                givenTree = givenTree.getParent();
                parent = parent.getParent();
            }
        }
        return splitPoint;
    }

    /**
     * Clear staging area.
     */
    public static void clearStagingArea() {
        HashMap<String, String> stage = new HashMap<>();
        Utils.writeObject(FORADDITION, stage);
        Utils.writeObject(FORREMOVAL, stage);
    }

    /**
     * @return GitTree gitTree.
     */
    public static GitTree getGitTree() {
        if (GITTREE.exists()) {
            return Utils.readObject(GITTREE, GitTree.class);
        } else {
            System.out.println("No such file or directory.");
            System.exit(0);
            return null;
        }
    }

    /**
     * @return String[] headPointer.
     */
    public static String[] getHeadPointer() {
        if (HEADPOINTER.exists()) {
            return Utils.readObject(HEADPOINTER, String[].class);
        } else {
            System.out.println("No such file or directory.");
            System.exit(0);
            return null;
        }
    }

    /**
     * @return String headPointerID.
     */
    public static String getHeadPointerID() {
        return getHeadPointer()[0];
    }

    /**
     * @return String headPointerBranch.
     */
    public static String getHeadPointerBranch() {
        return getHeadPointer()[1];
    }

    /**
     * @return HashMap<String, Commit> CommitHash.
     */
    public static HashMap<String, Commit> getCommitHash() {
        if (COMMITHASH.exists()) {
            return Utils.readObject(COMMITHASH, HashMap.class);
        } else {
            exit("No such file or directory.");
            return null;
        }
    }

    /** @return HashMap<String, String> forAddition. */
    public static HashMap<String, String> getForAddition() {
        if (FORADDITION.exists()) {
            return Utils.readObject(FORADDITION, HashMap.class);
        } else {
            exit("No such file or directory.");
            return null;
        }
    }

    /** @return HashMap<String, String> forRemoval. */
    public static HashMap<String, String> getForRemoval() {
        if (FORREMOVAL.exists()) {
            return Utils.readObject(FORREMOVAL, HashMap.class);
        } else {
            exit("No such file or directory.");
            return null;
        }
    }

    /** @return HashMap<File, Boolean> trackerHash. */
    public static HashMap<String, Boolean> getTrackerHash() {
        if (TRACKERHASH.exists()) {
            return Utils.readObject(TRACKERHASH, HashMap.class);
        } else {
            exit("No such file or directory.");
            return null;
        }
    }

    /** @return HashMap<String, byte[]> blobHash. */
    public static HashMap<String, byte[]> getBlobHash() {
        if (BLOBHASH.exists()) {
            return Utils.readObject(BLOBHASH, HashMap.class);
        } else {
            exit("No such file or directory.");
            return null;
        }
    }

    /** @return Commit currentCommit. */
    public static Commit getCurrentCommit() {
        Commit current = null;
        HashMap<String, Commit> commitHashMap = getCommitHash();
        if (getHeadPointer() != null) {
            current = commitHashMap.get(GitTree.find(getGitTree(),
                    getHeadPointer()[0]).getCurrentTreeCommit());
        } else {
            exit("No such file or directory.");
            return null;
        }
        return current;
    }

    /** @return HashMap<String, Commit> branchMap. */
    public static HashMap<String, Commit> getBranchMap() {
        if (BRANCHMAP.exists()) {
            return Utils.readObject(BRANCHMAP, HashMap.class);
        } else {
            exit("No such file or directory.");
            return null;
        }
    }

    /** @param branch String.
     * @param pointer Commit. */
    public static void setBranchMap(String branch, Commit pointer) {
        HashMap<String, Commit> branchMap = getBranchMap();
        branchMap.put(branch, pointer);
        Utils.writeObject(BRANCHMAP, branchMap);
    }

    /** @param pointer String[]. */
    public static void setHeadPointer(String[] pointer) {
        Utils.writeObject(HEADPOINTER, pointer);
    }

    /** @param id String. */
    public static void setHeadPointerID(String id) {
        String[] pointer = Utils.readObject(HEADPOINTER, String[].class);
        pointer[0] = id;
        Utils.writeObject(HEADPOINTER, pointer);
    }

    /** @param branch String. */
    public static void setHeadPointerBranch(String branch) {
        String[] pointer = Utils.readObject(HEADPOINTER, String[].class);
        pointer[1] = branch;
        Utils.writeObject(HEADPOINTER, pointer);
    }

    /** @param id String.
     * @param blob byte[]. */
    public static void setBlobHash(String id, byte[] blob) {
        HashMap<String, byte[]> blobs = getBlobHash();
        blobs.put(id, blob);
        Utils.writeObject(BLOBHASH, blobs);
    }

    /**@param id String.
     * @param current Commit.
     */
    public static void setCommitHash(String id, Commit current) {
        HashMap<String, Commit> commits = getCommitHash();
        commits.put(id, current);
        Utils.writeObject(COMMITHASH, commits);
    }

    /** @param file String.
     * @param id String. */
    public static void setForAddition(String file, String id) {
        HashMap<String, String> stage = getForAddition();
        stage.put(file, id);
        Utils.writeObject(FORADDITION, stage);
    }

    /** @param file String.
     * @param id String. */
    public static void setForRemoval(String file, String id) {
        HashMap<String, String> stage = getForRemoval();
        stage.put(file, id);
        Utils.writeObject(FORREMOVAL, stage);
    }

    /** @param file String.
     * @param bool boolean. */
    public static void setTrackerHash(String file, boolean bool) {
        HashMap<String, Boolean> tracker = getTrackerHash();
        tracker.put(file, bool);
        Utils.writeObject(TRACKERHASH, tracker);
    }

    /** @param commit .*/
    public static void setGitTree(Commit commit) {
        GitTree tree = getGitTree();
        tree.add(commit);
        Utils.writeObject(GITTREE, tree);
    }

    /** Print current commit.
     * @param commit Commit. */
    public static void printNode(Commit commit) {
        Commit current = commit;
        System.out.println("===");
        System.out.println("commit " + current.getRefId());
        System.out.println("Date: " + current.getTimeStamp());
        System.out.println(current.getLogMessage());
        System.out.println();
        if (current.getparentID() != null
                && current.getparentID().length() > 1) {
            return;
        }
    }

    /** Print current commit.
     * @param tree GitTree. */
    public static void printNode(GitTree tree) {
        printNode(getCommitHash().get(tree.getCurrentTreeCommit()));
    }

    /** @param tree to traverse to print all nodes. */
    public static void printAll(GitTree tree) {
        printNode(tree);
        for (GitTree child : tree.getChildren()) {
            printAll(child);
        }
    }

    /** to print all commits ever made. */
    public static void printAll() {
        HashMap<String, Commit> commitHashMap = getCommitHash();
        for (Map.Entry<String, Commit> com : commitHashMap.entrySet()) {
            printNode(com.getValue());
        }
    }

}
