package com.sample.bottom_project;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;


public class SearchArticleFragment extends Fragment implements AdapterView.OnItemClickListener {

    public static boolean run_check;
    private boolean visible;
    private SearchView searchView;
    private ListView listView;
    private ArrayList<String> list;
    private ArrayAdapter<String> adapter;
    private TextView realTimeSearch;
    private Button btn_wordcloud;

    private static String KEY_WORD;

    private String URL_NUM = "https://news.joins.com/Search/JoongangNews?page=";
    private String URL_WORD = "&Keyword=";
    private String URL_REST = "&SortType=New&SearchCategoryType=JoongangNews";
    private String htmlPageUrl = "https://datalab.naver.com/keyword/realtimeList.naver";
    private String htmlContentInStringFormat;

    private String[] word_array;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        final View SearchArticleView = inflater.inflate(R.layout.fragment_searcharticle, container, false);
        searchView = (SearchView) SearchArticleView.findViewById(R.id.searchView);
        listView = (ListView) SearchArticleView.findViewById(R.id.listView);
        realTimeSearch = (TextView) SearchArticleView.findViewById(R.id.realTimeSearch);
        btn_wordcloud = (Button) SearchArticleView.findViewById(R.id.btn_wordcloud);

        list = new ArrayList<>();

        JsoupAsyncTask jsoupAsyncTask = new JsoupAsyncTask();
        jsoupAsyncTask.execute();

        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, list);
        listView.setOnItemClickListener(this);
        listView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (run_check==false){
                    htmlContentInStringFormat="";
                    htmlPageUrl = URL_NUM + 1 + URL_WORD + query + URL_REST;
                    KEY_WORD = query;
                    realTimeSearch.setVisibility(View.GONE);
                    JsoupAsyncTask jsoupAsyncTask = new JsoupAsyncTask();
                    jsoupAsyncTask.execute();
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (run_check==false){
                    htmlPageUrl = "https://datalab.naver.com/keyword/realtimeList.naver";
                    realTimeSearch.setVisibility(View.VISIBLE);
                    btn_wordcloud.setVisibility(View.GONE);
                    JsoupAsyncTask jsoupAsyncTask = new JsoupAsyncTask();
                    jsoupAsyncTask.execute();
                }
                return false;
            }
        });
        return SearchArticleView;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //item 값을 가져와서 셋팅
        String vo = (String)adapterView.getAdapter().getItem(i);
        searchView.setQuery(vo, true);
    }

    private class JsoupAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                run_check=true;
                list.clear();
                Elements links;
                Document doc = Jsoup.connect(htmlPageUrl).get();
                if(htmlPageUrl == "https://datalab.naver.com/keyword/realtimeList.naver") {
                    visible=false;
                    links = doc.select("span.item_title");
                    for (Element link : links) {
                        list.add(link.text().trim());
                    }
                } else {
                    visible=true;
                    for (Element ARTICLE_URL : doc.select(".headline.mg a")) {
                        try{
                            htmlContentInStringFormat += crawling(ARTICLE_URL.attr("abs:href").toString());
                        }catch(IOException e) {
                            list.add("오류(002) - 오류 코드와 함께 문의 부탁드립니다.");
                            e.printStackTrace();
                        }
                    }
                    Document doc2 = null;
                    try{
                        doc2 = Jsoup.connect("http://192.168.0.3:5000/?word="+KEY_WORD
                                +"&name="+ URLEncoder.encode(htmlContentInStringFormat,"UTF-8")).get();

                        word_array = doc2.text().toString().split(" ");

                        for(int i = 0; i < word_array.length; i++) {
                            if(word_array[0] == "") {
                                list.add("검색결과가 없습니다.");
                                break;
                            }
                            if (i%2==0){
                                list.add(word_array[i]);
                            }
                            if(i == 30) {
                                break;
                            }
                        }
                    } catch(Exception e) {
                        list.clear();
                        list.add("오류(003) - 오류 코드와 함께 문의 부탁드립니다.");
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                list.clear();
                list.add("오류(001) - 오류 코드와 함께 문의 부탁드립니다.");
                e.printStackTrace();
            }
            run_check=false;
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            // 새로고침
            adapter.notifyDataSetChanged();
            if(visible==true) {
                btn_wordcloud.setVisibility(View.VISIBLE);
            }
            // 종료
            this.cancel(true);
        }
    }

    public static String crawling(String URL) throws IOException {
        Document doc = Jsoup.connect(URL).get();
        Elements elements = doc.select("#article_body");
        return elements.text().toString();
    }
}

