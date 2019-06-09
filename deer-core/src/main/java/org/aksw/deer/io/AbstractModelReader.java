package org.aksw.deer.io;

import org.jetbrains.annotations.NotNull;

/**
 *
 *
 *
 */
public abstract class AbstractModelReader extends AbstractModelIO implements ModelReader {

  @NotNull
  @Override
  public DegreeBounds getDegreeBounds() {
    return new DegreeBounds(0,0,1,1);
  }

}
