package com.driva;

import io.lettuce.core.dynamic.Commands;

public interface ClientCommands extends Commands {
    int getCacheSize();
}
