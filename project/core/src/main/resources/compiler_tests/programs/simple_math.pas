program Simple_Math;

var 
    num1: real;
    num2: real;
    result: real;

begin
    num1 := 2.0;
    num2 := 3.0;
    write ('2 + 3 = ');
    result := num1 + num2;
    writeln (result);
    write ('2 - 3 = ');
    result := num1 - num2;
    writeln (result);
    write ('2 * 3 = ');
    result := num1 * num2;
    writeln (result);
    write ('2 / 3 = ');
    result := num1 / num2;
    writeln (result);
end.