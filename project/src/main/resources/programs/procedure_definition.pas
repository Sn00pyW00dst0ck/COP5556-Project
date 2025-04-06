program Procedure_Definition;
var
    a, b, c: integer;
    output : integer;

procedure display;
var
    a, b, c: integer;
begin
    a := 10;
    b := 20;
    c := a + b;

    writeln('Within the procedure display');
    write('value of a = ', a);
    write(' b = ',  b);
    writeln(' and c = ', c);
end;

procedure parameter_display(x, y: integer);
var
    a, b, c: integer;
begin
    a := x;
    b := y;
    c := a + b;

    writeln('Within the procedure parameter_display');
    write('value of a = ', a);
    write(' b = ',  b);
    writeln(' and c = ', c);
end;

procedure SumByRef(a, b, c : integer; var m : integer);
begin
    m := a + b + c
end;

begin
    a:= 100;
    b:= 200;
    c:= a + b;

    writeln('Within the program');
    write('value of a = ', a);
    write(' b = ',  b);
    writeln(' and c = ', c);
    display();
    parameter_display(15, 45);

    output := 0;
    SumByRef(a, b, c, output);
    writeln('Modifying value within function');
    writeln (output);
end.