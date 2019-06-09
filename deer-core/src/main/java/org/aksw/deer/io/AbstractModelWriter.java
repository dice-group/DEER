package org.aksw.deer.io;

import org.jetbrains.annotations.NotNull;

/**
 *
 *
 *
 */
public abstract class AbstractModelWriter extends AbstractModelIO implements ModelWriter {

  @NotNull
  @Override
  public DegreeBounds getDegreeBounds() {
    return new DegreeBounds(1,1,0,1);
  }

}
