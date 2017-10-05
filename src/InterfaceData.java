
/*
 * Copyright (c) 2017 Viacheslav Babanin.
 * Contact email: babanin.vb.vb@gmail.com
 * This file is a part of Samba Active Directory Express (SADE).
 *
 * Samba Active Directory Express (SADE) is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Samba Active Directory Express (SADE)  is distributed
 * in the hope that it will be useful,but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

public class InterfaceData
{
    String name;
    int startPos;
    int endPos;

    public InterfaceData(String name, int startPos, int endPos)
    {
        this.name = name;
        this.startPos = startPos;
        this.endPos = endPos;
    }
}
