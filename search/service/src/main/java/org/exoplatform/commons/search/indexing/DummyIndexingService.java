package org.exoplatform.commons.search.indexing;

import java.util.Map;

import org.exoplatform.commons.api.indexing.IndexingService;
import org.exoplatform.commons.api.indexing.data.SearchEntry;
import org.exoplatform.commons.api.indexing.data.SearchEntryId;

public class DummyIndexingService extends IndexingService {

  public void add(SearchEntry searchEntry) {
    // Nothing to index
  }

  public void update(SearchEntryId id, Map<String, Object> changes) {
    // Nothing to index
  }

  public void delete(SearchEntryId id) {
    // Nothing indexed
  }
}
