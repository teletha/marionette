/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package marionette.macro;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.DirectoryChooser;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import kiss.Storable;
import viewtify.ActivationPolicy;
import viewtify.Theme;
import viewtify.Viewtify;
import viewtify.ui.UI;
import viewtify.ui.UICheckBox;
import viewtify.ui.UIListView;
import viewtify.ui.View;
import viewtify.ui.helper.User;

public class EasyMacro extends View {

    private final MacroManager directories = I.make(MacroManager.class);

    private UIListView<AbstractMacro> list;

    class view extends UI {
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
            c.menu().text("Restart").when(User.LeftClick, Viewtify::reactivate);
        }).renderByUI(e -> make(UICheckBox.class).text(e.name()).sync(e.enable));
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

        private URLClassLoader loader;

        private ObservableList<AbstractMacro> macros = FXCollections.observableArrayList();

        /**
         * 
         */
        protected MacroManager() {
            restore();
        }

        /**
         * Create new classloader for macro.
         * 
         * @return
         */
        private void loadMacro() {
            if (loader != null) {
                try {
                    for (AbstractMacro macro : macros) {
                    }
                    macros.clear();
                    loader.close();
                } catch (IOException e) {
                    throw I.quiet(e);
                }
            }
            loader = new URLClassLoader(I.signal(directories).map(path -> path.toURI().toURL()).toList().toArray(URL[]::new));

            // scan
            ScanResult result = new ClassGraph().enableAllInfo().addClassLoader(loader).scan();

            for (ClassInfo info : result.getSubclasses(AbstractMacro.class.getName())) {
                if (!info.isAbstract()) {
                    try {
                        Class<AbstractMacro> clazz = (Class<AbstractMacro>) Class.forName(info.getName(), true, loader);
                        AbstractMacro macro = I.make(clazz);
                        macro.declare();

                        macros.add(macro);
                    } catch (ClassNotFoundException e) {
                        throw I.quiet(e);
                    }
                }
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
                .size(70, 150)
                .icon("marionette/macro/icon.png")
                .onTerminating(GlobalEvents::disposeNativeHook)
                .activate(EasyMacro.class);
    }
}
