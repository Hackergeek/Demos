package com.yl.dragfillblankquestiondemo;

import android.graphics.Bitmap;

import java.util.List;

public class DragImageQuestion extends Question{



    List<ImageInput> inputList;
    List<OptionInput> optionList;



    static class ImageInput {
        Bitmap bitmap;
        int inputIndex;
    }

    static class OptionInput {
        Bitmap bitmap;
        int optionIndex;
    }
}
