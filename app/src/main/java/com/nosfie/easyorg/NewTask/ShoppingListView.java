package com.nosfie.easyorg.NewTask;

import android.content.Context;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.nosfie.easyorg.Helpers.ViewHelper;
import com.nosfie.easyorg.R;

import static com.nosfie.easyorg.Helpers.ViewHelper.convertDpToPixels;

public class ShoppingListView {

    private static int DP = 0;

    public static TableRow getShoppingItemRow(final Context context, int num, String value) {

        if (DP == 0)
            DP = ViewHelper.convertDpToPixels(context, 1);

        TableRow row = new TableRow(context);
        TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 10 * DP, 0, 0);
        row.setLayoutParams(params);
        row.setId(num);

        LinearLayout linearRow = new LinearLayout(context);
        TableRow.LayoutParams linearParams = new TableRow.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        linearParams.weight = 1;
        linearRow.setLayoutParams(linearParams);
        linearRow.setGravity(Gravity.CENTER_VERTICAL);
        linearRow.setOrientation(LinearLayout.HORIZONTAL);

        final TextView number = new TextView(context);
        LinearLayout.LayoutParams numberParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        numberParams.width = 30 * DP;
        numberParams.setMargins(15 * DP, 0, 0, 0);
        number.setPadding(0, 0, 10 * DP, 0);
        number.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        number.setLayoutParams(numberParams);
        number.setText(Integer.toString(num));
        number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout parent = (LinearLayout)(view.getParent()).getParent();
                Toast.makeText(context, Integer.toString(parent.getId()),
                        Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayout itemContainer = new LinearLayout(context);
        LinearLayout.LayoutParams itemContainerParams =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        itemContainerParams.width = 0;
        itemContainerParams.weight = 1;
        itemContainer.setOrientation(LinearLayout.HORIZONTAL);
        itemContainer.setLayoutParams(itemContainerParams);

        EditText item = new EditText(context);
        LinearLayout.LayoutParams itemParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        itemParams.setMargins(0, 0,
                ViewHelper.convertDpToPixels(context, 15), 0);
        item.setPadding(
                ViewHelper.convertDpToPixels(context, 8),
                ViewHelper.convertDpToPixels(context, 5),
                ViewHelper.convertDpToPixels(context, 5),
                ViewHelper.convertDpToPixels(context, 5)
        );
        item.setSingleLine(true);
        item.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        item.setBackgroundResource(R.drawable.border_small);
        item.setTextColor(0xFF333333);
        item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        item.setLayoutParams(itemParams);
        item.setText(value);

        ImageView deleteIcon = new ImageView(context);
        deleteIcon.setImageResource(R.drawable.delete_icon_small);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        iconParams.setMargins(0, 0, convertDpToPixels(context, 15), 0);
        iconParams.height = 28 * DP;
        deleteIcon.setLayoutParams(iconParams);
        deleteIcon.setAdjustViewBounds(true);
        itemContainer.addView(item);

        linearRow.addView(number);
        linearRow.addView(itemContainer);
        linearRow.addView(deleteIcon);

        row.addView(linearRow);

        return row;
    }

}