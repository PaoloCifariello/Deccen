package p2p.deccen.core.values;

import peersim.core.Protocol;
import java.util.ArrayList;

/**
 * Created by paolocifariello.
 */
public class DoubleVectorHolder<T, S> implements Protocol {
    protected ArrayList<T> vec1 = new ArrayList<>();
    protected ArrayList<S> vec2 = new ArrayList<>();

    public Object clone() {
        DoubleVectorHolder var1 = null;

        try {
            var1 = (DoubleVectorHolder) super.clone();
        } catch (CloneNotSupportedException var3) {
            ;
        }

        return var1;
    }

    public ArrayList<T> getFirstValue() {
        return this.vec1;
    }

    public void setFirstValue(ArrayList<T> var1) {
        this.vec1 = var1;
    }

    public ArrayList<S> getSecondValue() {
        return this.vec2;
    }

    public void setSecondValue(ArrayList<S> var1) {
        this.vec2 = var1;
    }
}