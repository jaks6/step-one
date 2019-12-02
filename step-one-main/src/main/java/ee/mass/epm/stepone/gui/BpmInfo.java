package ee.mass.epm.stepone.gui;


import java.util.ArrayList;
import java.util.List;

/**
 * Class for storing process engine related information in a tree form for
 * user interface(s).
 */
public class BpmInfo {
    private String text;
    private List<BpmInfo> moreInfo = null;

    /**
     * Creates a process info based on a text.
     * @param infoText The text of the info
     */
    public BpmInfo(String infoText) {
        this.text = infoText;
    }

    /**
     * Creates a process info based on any object. Object's
     * toString() method's output is used as the info text.
     * @param o The object this info is based on
     */
    public BpmInfo(Object o) {
        this.text = o.toString();
    }

    /**
     * Adds child info object for this process info.
     * @param info The info object to add.
     */
    public void addMoreInfo(BpmInfo info) {
        if (this.moreInfo == null) { // lazy creation
            this.moreInfo = new ArrayList<BpmInfo>();
        }
        this.moreInfo.add(info);
    }

    /**
     * Returns the process routing infos of this info.
     * @return The children of this info or an empty list if this info
     * doesn't have any children.
     */
    public List<BpmInfo> getMoreInfo() {
        if (this.moreInfo == null) {
            return new ArrayList<BpmInfo>(0);
        }
        return this.moreInfo;
    }

    /**
     * Returns the info text of this routing info.
     * @return The info text
     */
    public String toString() {
        return this.text;
    }

}
