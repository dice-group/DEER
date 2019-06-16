package org.aksw.deer.io;

/**
 *
 *
 *
 */
public abstract class AbstractModelReader extends AbstractModelIO implements ModelReader {

  @Override
  public DegreeBounds getDegreeBounds() {
    return new DegreeBounds(0,0,1,1);
  }

}
