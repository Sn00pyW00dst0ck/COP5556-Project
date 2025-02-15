program Comparison_Operators;
begin
  writeln (4 > 3);
  writeln (4 < 3);
  writeln (6 <> 2);
  writeln (3 <= 4);
  writeln (3 = 3);
  writeln (3.5 = 4);
  writeln (3.5 < 4);

  writeln ('Hello' < 'World');
  writeln ('Hello' <= 'World');
  writeln ('Hello' <> 'World');
  writeln ('Hello' = 'World');
  writeln ('Hello' = 'Hello');

  writeln (true < false);
  writeln (false <= true);

  writeln (CHR(63) < CHR(65));
  writeln (CHR(63) = CHR(65));
  writeln (CHR(63) <> CHR(65));
end.