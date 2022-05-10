package com.breezedevs.shopmobile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.breezedevs.shopmobile.databinding.ActivityDocumentBinding;
import com.breezedevs.shopmobile.databinding.ItemDocumentListBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityDocument extends ActivityClass {

    private ActivityDocumentBinding _b;
    private DocumentsListAdapter mDocumentsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _b = ActivityDocumentBinding.inflate(getLayoutInflater());
        _b.llBack.setOnClickListener(this);
        _b.btnCreate.setOnClickListener(this);
        _b.txtCreate.setOnClickListener(this);
        _b.txtStoreInputDoc.setOnClickListener(this);
        _b.txtStoreOutputDoc.setOnClickListener(this);
        _b.txtStoreMovementDoc.setOnClickListener(this);
        _b.txtCancel.setOnClickListener(this);
        mViewExpandCollapse.collapseMenu(_b.llDocType);

        mDocumentsListAdapter = new DocumentsListAdapter();
        _b.rv.setLayoutManager(new GridLayoutManager(this, 1));
        _b.rv.setAdapter(mDocumentsListAdapter);
        setContentView(_b.getRoot());
    }

    @Override
    protected void onStart() {
        super.onStart();
        createProgressDialog();
        MessageMaker messageMaker = new MessageMaker(MessageList.dll_op);
        messageMaker.putString("rwshop");
        messageMaker.putString(Preference.getString("server_database"));
        messageMaker.putByte(DllOp.op_documents_list);
        messageMaker.putInteger(Integer.valueOf(Preference.getString("server_storecode")));
        sendMessage(messageMaker);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.llBack:
                finish();
                break;
            case R.id.btnCreate:
            case R.id.txtCreate:
                mViewExpandCollapse.expandMenu(_b.llDocType);
                break;
            case R.id.txtStoreInputDoc:
                createStoreDoc(1);
                break;
            case R.id.txtStoreOutputDoc:
                createStoreDoc(2);
                break;
            case R.id.txtStoreMovementDoc:
                createStoreDoc(3);
                break;
            case R.id.txtCancel:
                mViewExpandCollapse.collapseMenu(_b.llDocType);
                break;
        }
    }

    @Override
    protected void messageHandler(Intent intent) {
        dismissProgressDialog();
        if (intent.getBooleanExtra(MessageMaker.NETWORK_ERROR, false)) {
            DialogClass.error(this, getString(R.string.network_error));
            return;
        }
        switch (intent.getShortExtra("type", (short) 0)) {
            case MessageList.dll_op:
                byte[] data = intent.getByteArrayExtra("data");
                MessageMaker mm = new MessageMaker(MessageList.utils);
                byte op = mm.getByte(data);
                switch (op) {
                    case 0:
                    case 1:
                    case 2:
                        DialogClass.error(this, mm.getString(data));
                        break;
                    case DllOp.op_documents_list:
                        int row = mm.getInt(data);
                        mDocumentsListAdapter.mData.clear();
                        for (int i = 0; i < row; i++) {
                            DocRow dr = new DocRow();
                            dr.id = mm.getString(data);
                            dr.type = mm.getInt(data);
                            dr.typeName = mDocumentsListAdapter.mDocumentTypes.get(dr.type);
                            mDocumentsListAdapter.mData.add(dr);
                        }
                        mDocumentsListAdapter.notifyDataSetChanged();
                        break;
                }
        }
    }

    private void createStoreDoc(int docType) {
        mViewExpandCollapse.collapseMenu(_b.llDocType);
        Intent intentDocument = new Intent(this, ActivityEditDocument.class);
        intentDocument.putExtra("type", docType);
        intentDocument.putExtra("new", true);
        intentDocument.putExtra("id", "");
        startActivity(intentDocument);
    }

    private ViewExpandCollapse mViewExpandCollapse = new ViewExpandCollapse(new ViewExpandCollapse.ViewExpandCollapseListener() {
        @Override
        public void beforeExpand() {

        }

        @Override
        public void beforeCollapse() {

        }

        @Override
        public void expanded(int id) {

        }

        @Override
        public void collapsed(int id) {

        }
    });

    public class DocRow {
        String id;
        int type;
        String typeName;
    }

    private class DocumentsListAdapter extends RecyclerView.Adapter {

        public Map<Integer, String> mDocumentTypes = new HashMap();
        public List<DocRow> mData = new ArrayList();

        public DocumentsListAdapter() {
            mDocumentTypes.put(1, ActivityDocument.this.getString(R.string.store_input_document));
            mDocumentTypes.put(2, ActivityDocument.this.getString(R.string.store_output_document));
            mDocumentTypes.put(3, ActivityDocument.this.getString(R.string.store_movement_document));
        }

        private class VH extends RecyclerView.ViewHolder implements View.OnClickListener {

            private ItemDocumentListBinding _i;

            public VH(ItemDocumentListBinding i) {
                super(i.getRoot());
                _i = i;
                _i.getRoot().setOnClickListener(this);
            }

            public void onBind(int position) {
                DocRow dr = mData.get(position);
                _i.txtName.setText(dr.typeName);
            }

            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                DocRow dr = mData.get(position);
                Intent intentDocument = new Intent(ActivityDocument.this, ActivityEditDocument.class);
                intentDocument.putExtra("type", 0);
                intentDocument.putExtra("new", false);
                intentDocument.putExtra("id", dr.id);
                startActivity(intentDocument);
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemDocumentListBinding i = ItemDocumentListBinding.inflate(getLayoutInflater());
            return new VH(i);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((VH) holder).onBind(position);
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }
}