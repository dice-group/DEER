package org.aksw.deer.io;

/**
 *
 *
 *
 */
public abstract class AbstractModelReader extends AbstractModelIO {

  @Override
  public DegreeBounds getDegreeBounds() {
    return new DegreeBounds(0,0,1,1);
  }

}
