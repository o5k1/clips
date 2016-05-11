package com.leaf.clips.di.modules;

import android.content.Context;
import dagger.internal.Factory;
import javax.annotation.Generated;

@Generated("dagger.internal.codegen.ComponentProcessor")
public final class AppModule_ProvidesContextFactory implements Factory<Context> {
  private final AppModule module;

  public AppModule_ProvidesContextFactory(AppModule module) {  
    assert module != null;
    this.module = module;
  }

  @Override
  public Context get() {  
    Context provided = module.providesContext();
    if (provided == null) {
      throw new NullPointerException("Cannot return null from a non-@Nullable @Provides method");
    }
    return provided;
  }

  public static Factory<Context> create(AppModule module) {  
    return new AppModule_ProvidesContextFactory(module);
  }
}

