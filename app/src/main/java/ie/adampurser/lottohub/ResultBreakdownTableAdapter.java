package ie.adampurser.lottohub;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ResultBreakdownTableAdapter extends RecyclerView.Adapter<ResultBreakdownTableAdapter.TableViewHolder>{
    private Context mContext;
    private Result[] mCompleteResult;

    public static class TableViewHolder extends RecyclerView.ViewHolder {
        LinearLayout container;
        TextView title;
        TableLayout table;

        public TableViewHolder(View v) {
            super(v);

            container = (LinearLayout) v.findViewById(R.id.linLayoutResultBreakdownTable);
            title = (TextView) v.findViewById(R.id.resultBreakdownTableTitle);
            table = (TableLayout) v.findViewById(R.id.resultBreakdownTable);
        }

        public void bindTable(Result result, Context context) {
            title.setText(result.getDrawType());

            String[] columnMatch = result.getMatches();
            String[] columnWinners = result.getWinners();
            String[] columnPrize = result.getPrizes();

            TextView tableValue;

            TableRow tableRow;
            for(int i = 0; i < columnMatch.length; i++) {
                tableRow = (TableRow) LayoutInflater.from(context)
                        .inflate(R.layout.result_breakdown_table_row, table, false);
                if(i % 2 == 0) {
                    tableRow.setBackgroundColor(context.getResources()
                            .getColor(R.color.result_breakdown_table_contrast_row));
                }

                tableValue = (TextView)tableRow.findViewById(R.id.tableValueMatch);
                tableValue.setText(columnMatch[i]);

                table.addView(tableRow);
            }

            int rowNum = 1;
            if(columnWinners != null) {
                for(int i = 0; i < columnMatch.length; i++) {
                    if(i == columnWinners.length) {
                        break;
                    }
                    tableValue = (TextView) table.getChildAt(rowNum)
                            .findViewById(R.id.tableValueWinners);
                    tableValue.setText(columnWinners[i]);
                    rowNum++;
                }
            }

            rowNum = 1;
            if(columnPrize != null) {
                for(int i = 0; i < columnMatch.length; i++) {
                    if(i == columnPrize.length) {
                        break;
                    }

                    tableValue = (TextView) table.getChildAt(rowNum)
                            .findViewById(R.id.tableValuePrize);
                    tableValue.setText(columnPrize[i]);
                    rowNum++;
                }
            }
        }
    }

    public ResultBreakdownTableAdapter(Context context, Result[] results) {
        mContext = context;
        mCompleteResult = results;
    }

    @Override
    public TableViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TableViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.result_breakdown_table, parent, false));
    }

    @Override
    public void onBindViewHolder(TableViewHolder holder, int position) {
        holder.bindTable(mCompleteResult[position], mContext);
    }

    @Override
    public int getItemCount() {
        return mCompleteResult.length;
    }
}
