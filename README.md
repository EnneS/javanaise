# Javanaise
Borne Jonathan, Surville Cleo, Soulier Nathan.

## Ce qui a été fait

- Javanaise v1
- Javanaise v2
- Data persistence (with filesystem)
- Server crash management
- Stress Tests (Write & Read+Write of 4 clients simultaneously and randomly)
- Test locks (IRC GUI with "Unlock" button)

## Install

- Generate javadoc with `mvn javadoc:javadoc`

The result is accessible in `target/site/apidocs/index.html`

- Build with `mvn install`.
- Run count test with `./test_count.sh` (`ctrl-c` to kill).
- Run fuzzing test with `./test_fuzz.sh`  (`ctrl-c` to kill). 
- Run client crash test with `./test_crash` (`ctrl-c` to kill).
- Run manual lock test with `./test_lockButton.sh` (`ctrl-c` to kill).
- Run persistance test with `./test_fuzz.sh` + `ctrl-c` +  `./test_fuzz2.sh`

## Tests
### test_count.sh

This test runs a coordinator and 4 clients collaborating to increment a shared counter object.
We simulate network latency by adding random thread.sleep() between writes.

#### Execution trace
```
Création de l'objet
[-54823616]Writing 1
[-54823616]Writing 2
[-54823616]Writing 4
[1022827455]Writing 3
[1661288118]Writing 5
[-54823616]Writing 6
[1661288118]Writing 7
[-1370755957]Writing 8
[1022827455]Writing 9
[-54823616]Writing 10
[1022827455]Writing 11
[1661288118]Writing 12
[1661288118]Writing 13
[-54823616]Writing 14
...
[-2062024572]Writing 497
[1022782379]Writing 498
[-1345717107]Writing 499
[1022782379]Writing 500
--->
[919217334]Writing 501
[-2062024572]Writing 502
[-1345717107]Writing 503

```

In the preceding trace we see the four clients
`[-54823616], [1022827455], [1661288118], [-1370755957]`
writing increasing values to the counter until 500 is reached.


### test_fuzz.sh

This test runs a coordinator and 4 clients collaborating to increment a shared counter object.
The clients randomly read and write the shared object.
We simulate network latency by adding random sleep between each write.

The goal is to verify concurent access doesn't result in interlock and coherency is preserved.

#### Execution trace:
```
Création de l'objet
[202963983]Writing 1
[202963983]Writing 2
[202963983]Writing 3
[202963983]Reading 3
[202963983]Reading 3
[-1777639701]Reading 3
[-1769140565]Reading 3
[866067996]Writing 4
[-1777639701]Reading 4
[202963983]Reading 4
[866067996]Writing 5
...
[202963983]Writing 51
[-1777639701]Reading 51
[-1777639701]Reading 51
fini
[866067996]Reading 51
[-1777639701]Writing 52
fini
fini
[866067996]Writing 53
fini
...
```
In the preceding trace we see the four clients
writing/reading increasing values to the counter until 50 is reached.

### test_fuzz2.sh

This test should be launched after `test_fuzz.sh`.
The clients will use coordinator persistance to resume counting from last value (50).

#### Execution trace:
```
[1385790371]Reading 51
[-675829915]Reading 51
[-593730288]Reading 51
[-593730288]Reading 51
[-675829915]Writing 52
[1385790371]Writing 53
[1385790371]Writing 54
[-593730288]Reading 54
[-675829915]Reading 54
[-593730288]Reading 54
[-675829915]Reading 54
[1385790371]Reading 54
[-593730288]Reading 54
[1385790371]Writing 55
[-593730288]Writing 56
```

### test_crash.sh

The test is similar to test_fuzz except the clients
have 1/100 chance to crash.

#### Execution trace:
```
[500385740]Writing 5
[500385740]Reading 5
[1836748502]Writing 6
[1836748502]Will cra[sh
1646009556]Reading 6
java.lang.ArithmeticException: / by zero
        at irc.IrcFuzzCrash.writeCrash(IrcFuzzCrash.java:136)
        at irc.IrcFuzzCrash.main(IrcFuzzCrash.java:54)
        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.base/java.lang.reflect.Method.invoke(Method.java:566)
        at org.codehaus.mojo.exec.ExecJavaMojo$1.run(ExecJavaMojo.java:282)
        at java.base/java.lang.Thread.run(Thread.java:829)
Error IrcCrash
/ by zero
JvnServer unreachable
JvnServer unreachable
JvnServer unreachable
JvnServer unreachable
JvnServer unreachable
JvnServer unreachable
JvnServer unreachable
JvnServer unreachable
JvnServer unreachable
JvnServer unreachable
[500385740]Reading 6
[1646009556]Writing 7
[500385740]Writing 8
[1086375534]Reading 7
[1646009556]Reading 8
```

In the preceding trace we see a client crash and another client waiting for the lock of the crashed client.
The coordinator tries to reach the client for the lock 10 times.
After timeout the client is considered crashed (crash-failure model) and
the remaining clients continue their counting.
