package com.yl.dragfillblankquestiondemo;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 拖拽填空题
 * Created by yangle on 2017/10/9.
 * <p>
 * Website：http://www.yangle.tech
 * <p>
 * GitHub：https://github.com/alidili
 * <p>
 * CSDN：http://blog.csdn.net/kong_gu_you_lan
 * <p>
 * JianShu：http://www.jianshu.com/u/34ece31cd6eb
 */

public class MainActivity2 extends AppCompatActivity {

    @BindView(R.id.dfbv_content)
    DragFillBlankImageView dfbvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ButterKnife.bind(this);

        initData();
    }

    private void initData() {
        DragImageQuestion dragImageQuestion = new DragImageQuestion();
        List<DragImageQuestion.ImageInput> list = new ArrayList<>();
        List<DragImageQuestion.OptionInput> optionInputs = new ArrayList<>();
        for (int i = 0; i < 5; ++i) {
            DragImageQuestion.ImageInput input = new DragImageQuestion.ImageInput();
            input.inputIndex = i;
            if (i != 4) {
                input.bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            }
            list.add(input);
        }
        for (int i = 0; i < 3; ++i) {
            DragImageQuestion.OptionInput option =  new DragImageQuestion.OptionInput();
            option.optionIndex = i;
            option.bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            optionInputs.add(option);
        }
        dragImageQuestion.optionList = optionInputs;
        dragImageQuestion.inputList = list;
        dfbvContent.setData(dragImageQuestion);
    }

    public void reset(View view) {
        dfbvContent.reset();
    }
}
