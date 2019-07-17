package com.modestie.modestieapp.activities;

import android.app.Dialog;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.adapters.ItemSearchAdapter;
import com.modestie.modestieapp.model.item.LightItem;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import static com.android.volley.Request.Method.GET;

public class ItemSelectionDialogFragment extends DialogFragment
{
    public static final String TAG = "DFRAG.ITEMSELECT";

    private View rootview;
    private Toolbar toolbar;

    private FrameLayout noContentPlaceholder;
    private ProgressBar noContentProgressBar;
    private TextView noContentLabel;
    private ImageView searchIcon;
    private ImageView previousIcon;
    private ImageView nextIcon;
    private TextView paginationView;

    private boolean searchLayoutElevated;

    private ArrayList<LightItem> dataset;
    private int currentPage;
    private int maxPages;
    private int results;
    private int resultsPerPage;
    private int resultsTotal;

    private LinearLayout searchLayout;
    private ConstraintLayout pagerLayout;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter<ItemSearchAdapter.GameItemViewHolder> adapter;
    private RecyclerView.LayoutManager layoutManager;

    private RequestQueue mRequestQueue;

    private OnItemSelectedListener callback;

    public ItemSelectionDialogFragment()
    {
        // Required empty public constructor
    }

    static ItemSelectionDialogFragment display(FragmentManager fragmentManager)
    {
        ItemSelectionDialogFragment dialog = new ItemSelectionDialogFragment();
        dialog.show(fragmentManager, TAG);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_ModestieTheme_FullScreenDialog);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.dialog_item_selection, container, false);

        this.toolbar = rootView.findViewById(R.id.toolbar);
        this.noContentPlaceholder = rootView.findViewById(R.id.noContentPlaceholder);
        this.noContentProgressBar = rootView.findViewById(R.id.noContentProgress);
        this.noContentLabel = rootView.findViewById(R.id.noContentLabel);
        this.searchIcon = rootView.findViewById(R.id.searchIcon);
        this.previousIcon = rootView.findViewById(R.id.pagePrevious);
        this.nextIcon = rootView.findViewById(R.id.pageNext);
        this.paginationView = rootView.findViewById(R.id.paginationView);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        this.rootview = view;

        this.toolbar.setTitle("Sélection d'item");
        this.toolbar.setNavigationOnClickListener(v -> dismiss());

        this.dataset = new ArrayList<>();

        this.searchLayout = this.rootview.findViewById(R.id.searchLayout);
        this.searchLayoutElevated = false;
        this.pagerLayout = this.rootview.findViewById(R.id.searchPagerLayout);

        this.previousIcon.setVisibility(View.INVISIBLE);
        this.nextIcon.setVisibility(View.INVISIBLE);

        this.recyclerView = this.rootview.findViewById(R.id.searchResultsLayout);
        this.layoutManager = new LinearLayoutManager(getContext());
        this.recyclerView.setLayoutManager(this.layoutManager);
        //Set adapter and call callback listener to return selected item
        this.adapter = new ItemSearchAdapter(dataset, item ->
        {
            callback.OnItemSelectedListener(item);
            dismiss();
        });

        this.recyclerView.setAdapter(this.adapter);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            Objects.requireNonNull(dialog.getWindow()).setLayout(width, height);
        }

        TextInputLayout searchField = this.rootview.findViewById(R.id.fieldItemSearch);
        this.currentPage = 1;

        searchField.getEditText().setOnEditorActionListener(
                (v, actionId, event) ->
                {
                    if (actionId == EditorInfo.IME_ACTION_DONE)
                    {
                        executeSearch(searchField, this.currentPage);
                    }
                    return false;
                });

        this.searchIcon.setOnClickListener(v -> executeSearch(searchField, this.currentPage));

        this.previousIcon.setOnClickListener(v -> executeSearch(searchField, --this.currentPage));
        this.nextIcon.setOnClickListener(v -> executeSearch(searchField, ++this.currentPage));
    }

    private void executeSearch(TextInputLayout field, int page)
    {
        String request = "https://xivapi.com/search?indexes=Item&language=fr&page=" + page + "&string=";
        request += Objects.requireNonNull(field.getEditText()).getText();

        field.getEditText().clearFocus();
        field.setEnabled(false);

        recyclerView.setVisibility(View.INVISIBLE);

        noContentPlaceholder.setAlpha(1f);
        noContentLabel.setVisibility(View.GONE);
        noContentProgressBar.setVisibility(View.VISIBLE);

        addToRequestQueue(new JsonObjectRequest(
                GET, request, null,
                response ->
                {
                    buildDatasetFromResponse(response);
                    adapter.notifyDataSetChanged();

                    recyclerView.setVisibility(View.VISIBLE);
                    noContentPlaceholder.animate().alpha(0f);
                    elevateSearchLayout();

                    try
                    {
                        JSONObject pagination = response.getJSONObject("Pagination");
                        if (pagination.optInt("PagePrev", 0) != 0)
                            this.previousIcon.setVisibility(View.VISIBLE);
                        else
                            this.previousIcon.setVisibility(View.INVISIBLE);
                        if (pagination.optInt("PageNext", 0) != 0)
                            this.nextIcon.setVisibility(View.VISIBLE);
                        else
                            this.nextIcon.setVisibility(View.INVISIBLE);

                        this.maxPages = pagination.getInt("PageTotal");
                        this.results = pagination.getInt("Results");
                        this.resultsPerPage = pagination.getInt("ResultsPerPage");
                        this.resultsTotal = pagination.getInt("ResultsTotal");

                        setPaginationTextView();
                    }
                    catch (JSONException e)
                    {
                        Log.e(TAG, e.getLocalizedMessage());
                    }

                    field.setEnabled(true);

                }, error ->
                {
                    noContentLabel.setVisibility(View.VISIBLE);
                    noContentProgressBar.setVisibility(View.GONE);
                    noContentLabel.setText(R.string.item_search_placeholder_error);
                    dropSearchLayout();
                    field.setEnabled(true);
                }));
    }

    private void elevateSearchLayout()
    {
        if (!searchLayoutElevated)
        {
            TransitionDrawable transition = (TransitionDrawable) searchLayout.getBackground();
            searchLayout.animate().translationZ(7f);
            transition.startTransition((int) searchLayout.animate().getDuration());
            searchLayoutElevated = true;
        }
    }

    private void dropSearchLayout()
    {
        if (searchLayoutElevated)
        {
            TransitionDrawable transition = (TransitionDrawable) searchLayout.getBackground();
            searchLayout.animate().translationZ(-7f);
            transition.reverseTransition((int) searchLayout.animate().getDuration());
            searchLayoutElevated = false;
        }
    }

    private void setPaginationTextView()
    {
        int first, last;

        first = (this.currentPage - 1) * this.resultsPerPage + 1;
        last = this.results + (first - 1);

        this.paginationView.setText(String.format(Locale.FRANCE, "%d-%d de %s", first, last, this.resultsTotal));

        /*StringBuilder prevPages = new StringBuilder(), nextPages = new StringBuilder();
        int p = this.currentPage, pCount = 0, n = this.currentPage, nCount = 0;
        while (p != 1 && pCount < 2)
        {
            prevPages.append(--p).append(" ");
            pCount++;
        }
        while (n != this.maxPages && nCount < 2)
        {
            nextPages.append(" ").append(++n);
            nCount++;
        }
        this.paginationView.setText(String.format(Locale.getDefault(), "%s %d %s", prevPages.toString(), this.currentPage, nextPages.toString()));*/
    }

    public void buildDatasetFromResponse(JSONObject obj)
    {
        try
        {
            JSONArray results = obj.getJSONArray("Results");

            this.dataset.clear();

            for (int i = 0; i < results.length(); i++)
            {
                this.dataset.add(new LightItem(results.getJSONObject(i)));
            }
        }
        catch (JSONException e)
        {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnItemSelectedListener
    {
        void OnItemSelectedListener(LightItem item);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener callback)
    {
        this.callback = callback;
    }

    /**
     * Lazy initialize the request queue, the queue instance will be created when it is accessed
     * for the first time
     *
     * @return Request Queue
     */
    public RequestQueue getRequestQueue()
    {
        if (this.mRequestQueue == null)
            this.mRequestQueue = Volley.newRequestQueue(Objects.requireNonNull(getContext()));

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag)
    {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);

        VolleyLog.d("Adding request to queue: %s", req.getUrl());

        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req)
    {
        // set the default tag if tag is empty
        req.setTag(TAG);

        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag)
    {
        if (mRequestQueue != null)
            mRequestQueue.cancelAll(tag);
    }
}
