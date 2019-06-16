package org.aksw.deer.io;

/**
 *
 *
 *
 */
public abstract class AbstractModelWriter extends AbstractModelIO implements ModelWriter {

  @Override
  public DegreeBounds getDegreeBounds() {
    return new DegreeBounds(1,1,0,1);
  }

}
