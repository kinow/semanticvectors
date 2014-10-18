package pitt.search.semanticvectors;

import java.util.Enumeration;
import java.util.Hashtable;

import pitt.search.semanticvectors.orthography.NumberRepresentation;
import pitt.search.semanticvectors.orthography.StringEdit;
import pitt.search.semanticvectors.vectors.Vector;

/**
 * This class provides methods for retrieving vectors that are computed
 * using the orthographical techniques in {@link NumberRepresentation}.
 * 
 * <p>
 * To save time, vectors are cached by default.
 * Methods exist to disable/enable caching and to clear the cache.
 * 
 * <p>
 * The serialization currently presumes that the object (in the ObjectVectors)
 * should be serialized as a String.
 * 
 * @see ObjectVector
 **/
public class VectorStoreOrthographical implements VectorStore, CloseableVectorStore {
  private FlagConfig flagConfig;
  private Hashtable<Object, ObjectVector> objectVectors;
  private boolean cacheVectors = true;
  private NumberRepresentation theNumbers;
  private StringEdit stringVectors;
  private VectorStore letterVectors;

  /**
   * Constructs a new orthographical vector store, defaulting to {@link VectorStoreDeterministic} for the
   * underlying letter vectors.
   */
  public VectorStoreOrthographical(FlagConfig flagConfig) {
    this(flagConfig, new VectorStoreDeterministic(flagConfig));
  }

  /**
   * Constructs a new vector store with the given arguments, initializing other data structures
   * including {@link NumberRepresentation} and {@link StringEdit}.
   * @param flagConfig
   * @param letterVectors
   */
  public VectorStoreOrthographical(FlagConfig flagConfig, VectorStore letterVectors) {
    this.flagConfig = flagConfig;
    this.letterVectors = letterVectors;
    this.objectVectors = new Hashtable<>();
    this.theNumbers = new NumberRepresentation(flagConfig);
    this.stringVectors = new StringEdit(flagConfig, theNumbers, letterVectors);
  }

  @Override
  public void close() {
    //dummy method to ease integration into existing command line query code
  }

  public Enumeration<ObjectVector> getAllVectors() {
    return this.objectVectors.elements();
  }

  @Override
  public int getNumVectors() {
    return this.objectVectors.size();
  }

  /**
   * Clear the vector cache.
   */
  public void clear() {
    objectVectors.clear();
  }

  /**
   * Enable or disable vector caching. Enabled cache speeds up repeated
   * querying of the same vector, but increases memory footprint. Cache can be
   * cleared with {@link #clear()}. By default the cache is enabled.
   * 
   * @param cacheVectors <code>true</code> to enable the cache,
   *        <code>false</code> otherwise
   */
  public void enableVectorCache(boolean cacheVectors) {
    this.cacheVectors = cacheVectors;
  }

  /**
   * Given an object, get its corresponding vector.
   * <p>
   * This implementation only works for string objects so far.
   * 
   * @param desiredObject the string you're searching for
   * @return vector from the VectorStore, or null if not found.
   * @throws NullPointerException if desiredObject or vector is
   *         <code>null</code>
   */
  public Vector getVector(Object desiredObject) throws NullPointerException {
    ObjectVector objectVector = this.objectVectors.get(desiredObject);
    if (objectVector != null) {
      return objectVector.getVector();
    } else {      
      Vector v = stringVectors.getStringVector(desiredObject.toString());

      if (cacheVectors) {
        objectVectors.put(desiredObject, new ObjectVector(desiredObject, v));
      }
      return v;
    }
  }



  @Override
  public boolean containsVector(Object object) {
	  return objectVectors.containsKey(object);
  }
  
}
