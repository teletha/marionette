/*
 * Copyright (C) 2024 The MARIONETTE Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package marionette;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.DirectoryChooser;
import kiss.Disposable;
import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import kiss.Storable;
import viewtify.ActivationPolicy;
import viewtify.Theme;
import viewtify.Viewtify;
import viewtify.ui.UICheckBox;
import viewtify.ui.UIListView;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.helper.User;

public class EasyMacro extends View {

    private final MacroManager directories = I.make(MacroManager.class);

    private UIListView<Macro> list;

    class view extends ViewDSL {
        {
            $(list);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        if (directories.directories.isEmpty()) {
            directories.directories.add(selectDirectory());
            directories.store();
        }

        directories.loadMacro();

        list.items(directories.macros).context(c -> {
            c.menu().text("Restart").when(User.LeftClick, () -> Viewtify.application().reactivate());
        }).renderByUI(() -> new UICheckBox(this), (check, e, disposer) -> check.text(e.name()).sync(e.enable));
    }

    private File selectDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("マクロを定義したクラスが存在するパッケージのルートディレクトリを選択して下さい");
        return directoryChooser.showDialog(findRootView().ui().sceneProperty().get().getWindow());
    }

    @Managed(value = Singleton.class)
    private static class MacroManager implements Storable {

        public List<File> directories = new ArrayList();

        public Map<Class, Boolean> enables = new HashMap();

        private Disposable loader;

        private ObservableList<Macro> macros = FXCollections.observableArrayList();

        /**
         * 
         */
        protected MacroManager() {
            restore();
        }

        /**
         * Create new classloader for macro.
         */
        private void loadMacro() {
            if (loader != null) {
                macros.clear();
                loader.dispose();
            }

            loader = I.signal(directories).map(path -> path.toURI().toURL()).to(I::load);

            for (Macro macro : I.find(Macro.class)) {
                macro.declare();
                macros.add(macro);
            }
        }
    }

    /**
     * Entry point.
     * 
     * @param args
     */
    public static void main(String[] args) {
        GlobalEvents.initializeNativeHook();

        Viewtify.application()
                .use(ActivationPolicy.Latest)
                .use(Theme.Dark)
                .onTerminating(GlobalEvents::disposeNativeHook)
                .activate(EasyMacro.class);
    }
}