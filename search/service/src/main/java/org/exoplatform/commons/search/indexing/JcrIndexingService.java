package org.exoplatform.commons.search.indexing;

import org.exoplatform.commons.api.indexing.IndexingService;
import org.exoplatform.commons.api.indexing.data.SearchEntry;
import org.exoplatform.commons.api.indexing.data.SearchEntryId;

import java.util.Map;

public class JcrIndexingService extends IndexingService {

  public void add(SearchEntry searchEntry) {
    // Nothing to do as all JCR data are automatically indexed by Lucene
  }

  public void update(SearchEntryId id, Map<String, Object> changes) {
    // Nothing to do as all JCR data are automatically indexed by Lucene
  }

  public void delete(SearchEntryId id) {
    // Nothing to do as all JCR data are automatically indexed by Lucene
  }
}
