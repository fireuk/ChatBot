package com.example.dialoguebot;

import java.util.ArrayList;
import java.util.List;

public class DialogueContent {

    /**
     * A dummy item representing a piece of content.
     */
    public static class DialogueItem {
        public String st_TalkContent;
        public String st_ReceiveContent;


        public DialogueItem(String st_TalkContent, String st_ReceiveContent) {
            this.st_TalkContent = st_TalkContent;
            this.st_ReceiveContent = st_ReceiveContent;
        }

        @Override
        public String toString() {
            return st_TalkContent;
        }
    }

    public static List<DialogueItem> ITEMS = new ArrayList<DialogueItem>();
    private static final int COUNT = 25;

    public static void addItem(DialogueItem dialogueItem){
        ITEMS.add(dialogueItem);
    }

    /*static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i+":: talk", i+":: receive"));
        }
    }*/

    public static DialogueItem createDialogueItem(String st_Talk, String st_Receive) {
        return new DialogueItem(st_Talk, st_Receive);
    }

}
