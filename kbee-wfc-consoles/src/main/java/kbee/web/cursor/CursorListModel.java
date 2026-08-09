package kbee.web.cursor;

import java.util.List;

import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.novamens.indexer.query.Cursor;
import com.novamens.indexer.query.SearchResult;

public class CursorListModel<T> implements Cursor, IDetachable {

    private static final long serialVersionUID = 1L;

    private List<IModel<T>> list;
    private int index = 0;

    public CursorListModel(List<IModel<T>> list, int index) {
        this.list = list;
        this.index = index;
    }

    @Override
    public long size() {
        if (list == null)
            return 0;
        return (long) list.size();
    }

    public List<IModel<T>> getList() {
        return list;
    }

    @Override
    public SearchResult get(long index) {
        return new ListModelSearchResult<T>(list.get((int) index));
    }

    @Override
    public SearchResult next() {
        if (index < list.size() - 1) {
            index = index + 1;
            return get(index);
        }
        return null;
    }

    @Override
    public SearchResult previous() {
        if (index > 0) {
            index = index - 1;
            return get(index);
        }
        return null;

    }

    @Override
    public boolean hasMoreElements() {
        return index < list.size() - 1;
    }

    @Override
    public void setIndex(long i) {

        if (i < 0)
            i = 0;
        if (i > list.size() - 1)
            i = list.size() - 1;
        this.index = (int) i;

    }

    @Override
    public long getIndex() {
        return index;
    }

    @Override
    public void detach() {
        if (list != null) {
            list.forEach(item -> item.detach());
        }

    }

}
