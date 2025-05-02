program LoopTest;

var
  i, sum: integer;

begin
  sum := 0;

  { Test FOR loop }
  for i := 1 to 5 do
    sum := sum + i;

  writeln('Sum after FOR loop (1 to 5): ', sum);

  { Test WHILE loop }
  i := 1;
  sum := 0;

  while i <= 5 do
  begin
    sum := sum + i;
    i := i + 1;
  end;

  writeln('Sum after WHILE loop (1 to 5): ', sum);
end.