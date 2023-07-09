package com.driva.server;

import io.lettuce.core.dynamic.Commands;

public interface ClientCommands extends Commands {
    int getCacheSize();
}
