package org.janusgraph.graphdb.vertices;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import org.janusgraph.diskstorage.Entry;
import org.janusgraph.diskstorage.EntryList;
import org.janusgraph.diskstorage.keycolumnvalue.SliceQuery;
import org.janusgraph.graphdb.internal.ElementLifeCycle;
import org.janusgraph.graphdb.internal.InternalRelation;
import org.janusgraph.graphdb.transaction.StandardTitanTx;
import org.janusgraph.graphdb.transaction.addedrelations.AddedRelationsContainer;
import org.janusgraph.graphdb.transaction.addedrelations.ConcurrentAddedRelations;
import org.janusgraph.graphdb.transaction.addedrelations.SimpleAddedRelations;
import org.janusgraph.util.datastructures.Retriever;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 */

public class StandardVertex extends AbstractVertex {

    private byte lifecycle;
    private volatile AddedRelationsContainer addedRelations=AddedRelationsContainer.EMPTY;

    public StandardVertex(final StandardTitanTx tx, final long id, byte lifecycle) {
        super(tx, id);
        this.lifecycle=lifecycle;
    }

    public synchronized final void updateLifeCycle(ElementLifeCycle.Event event) {
        this.lifecycle = ElementLifeCycle.update(lifecycle,event);
    }

    @Override
    public void removeRelation(InternalRelation r) {
        if (r.isNew()) addedRelations.remove(r);
        else if (r.isLoaded()) updateLifeCycle(ElementLifeCycle.Event.REMOVED_RELATION);
        else throw new IllegalArgumentException("Unexpected relation status: " + r.isRemoved());
    }

    @Override
    public boolean addRelation(InternalRelation r) {
        Preconditions.checkArgument(r.isNew());
        if (addedRelations==AddedRelationsContainer.EMPTY) {
            if (tx().getConfiguration().isSingleThreaded()) {
                addedRelations=new SimpleAddedRelations();
            } else {
                synchronized (this) {
                    if (addedRelations==AddedRelationsContainer.EMPTY)
                        addedRelations=new ConcurrentAddedRelations();
                }
            }
        }
        if (addedRelations.add(r)) {
            updateLifeCycle(ElementLifeCycle.Event.ADDED_RELATION);
            return true;
        } else return false;
    }

    @Override
    public List<InternalRelation> getAddedRelations(Predicate<InternalRelation> query) {
        return addedRelations.getView(query);
    }

    @Override
    public EntryList loadRelations(SliceQuery query, Retriever<SliceQuery, EntryList> lookup) {
        return (isNew()) ? EntryList.EMPTY_LIST : lookup.get(query);
    }

    @Override
    public boolean hasLoadedRelations(SliceQuery query) {
        return false;
    }

    @Override
    public boolean hasRemovedRelations() {
        return ElementLifeCycle.hasRemovedRelations(lifecycle);
    }

    @Override
    public boolean hasAddedRelations() {
        return ElementLifeCycle.hasAddedRelations(lifecycle);
    }

    @Override
    public synchronized void remove() {
        super.remove();
        ((StandardVertex)it()).updateLifeCycle(ElementLifeCycle.Event.REMOVED);
    }

    @Override
    public byte getLifeCycle() {
        return lifecycle;
    }
}
